/*
 * The sample smart contract for documentation topic:
 * Writing Your First Blockchain Application
 */

package main

/* Imports
 * 4 utility libraries for formatting, handling bytes, reading and writing JSON, and string manipulation
 * 2 specific Hyperledger Fabric specific libraries for Smart Contracts
 */
import (
	"encoding/json"
	"fmt"
	"strconv"
	"bytes"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	sc "github.com/hyperledger/fabric/protos/peer"
)

// 结算类
type Settlement struct {
	PurchaseCode  string `json:"purchasecode"`
	MaterialCode string `json:"materialcode"`
	SettlementMethod string `json:"settlementmethod"`
    SettlementDatetime string `json:"settlementdatatime"`
}

// 添加结算记录
func addSettlement(APIstub shim.ChaincodeStubInterface, args []string) bool {

	if len(args) != 5 {
		fmt.Println("Incorrect number of arguments. Expecting 5")
		return false
	}

	// 添加
	var settlement = Settlement{PurchaseCode:args[1], MaterialCode:args[2], SettlementMethod:args[3], SettlementDatetime:args[4]}
	settlementAsBytes, _ := json.Marshal(settlement)
	APIstub.PutState(args[0], settlementAsBytes)

	return true
}

// 获取结算记录数目
func getSettlementNumber(APIstub shim.ChaincodeStubInterface) int {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	return info.SettlementNumber
}

// 设置结算记录数目
func setSettlementNumber(APIstub shim.ChaincodeStubInterface, number int)  {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	info.SettlementNumber = number

	infoAsBytes, _ = json.Marshal(info)
    APIstub.PutState("INFO", infoAsBytes)
}

// 结算
func (s *SmartContract) settlement(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 4 {
		return shim.Error("Incorrect number of arguments. Expecting 4")
	}

	// 1. 添加结算记录
	// 查询结算number从而构建settlementID
	settlementNumber := getSettlementNumber(APIstub)
	settlementID := "SETTLEMENT" + strconv.Itoa(settlementNumber)

	if !addSettlement(APIstub, []string{settlementID, args[0], args[1], args[2], args[3]}) {
		return shim.Error("addSettlement error")
	}

	// 修改结算number
	setSettlementNumber(APIstub, settlementNumber + 1)

	// 2. 更新物料的结算信息
	// 先查询materialID
	materialID := getMaterialID(APIstub, []string{args[0], args[1]})
	if materialID == "" {
		return shim.Error("getMaterialID error")
	} 

	// 设置结算信息
	if !changeMaterialSettlement(APIstub, []string{materialID, args[2], args[3]}) {
		return shim.Error("changeMaterialSettlement error")
	}

	return shim.Success(nil)
}

// 查询所有结算记录
func (s *SmartContract) queryAllSettlements(APIstub shim.ChaincodeStubInterface) sc.Response {

	startKey := "SETTLEMENT0"
	endKey := "SETTLEMENT999"

	resultsIterator, err := APIstub.GetStateByRange(startKey, endKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	defer resultsIterator.Close()

	// buffer is a JSON array containing QueryResults
	var buffer bytes.Buffer
	buffer.WriteString("[")

	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return shim.Error(err.Error())
		}
		// Add a comma before array members, suppress it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		buffer.WriteString("{\"Key\":")
		buffer.WriteString("\"")
		buffer.WriteString(queryResponse.Key)
		buffer.WriteString("\"")

		buffer.WriteString(", \"Record\":")
		// Record is a JSON object, so we write as-is
		buffer.WriteString(string(queryResponse.Value))
		buffer.WriteString("}")
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")

	fmt.Printf("- queryAllSettlements:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}