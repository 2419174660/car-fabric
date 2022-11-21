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

// 采购类
type Purchase struct {
	PurchaseCode  string `json:"purchasecode"`
	MaterialCode string `json:"materialcode"`
	Description  string `json:"description"`
	Owner  string `json:"owner"`
    PurchaseNumber int `json:"purchasenumber"`
    Price float64 `json:"price"`
    Datetime string `json:"datetime"`
} 

// 添加采购记录
func addPurchase(APIstub shim.ChaincodeStubInterface, args []string) bool {

	if len(args) != 8 {
		fmt.Println("Incorrect number of arguments. Expecting 8")
		return false
	}

	// 添加
	purchaseNumber, err1 := strconv.Atoi(args[5])
	if err1 != nil {
		fmt.Println(err1.Error())
		return false
	}
	price, err2 := strconv.ParseFloat(args[6], 64)
	if err2 != nil {
		fmt.Println(err2.Error())
		return false
	}

	var purchase = Purchase{PurchaseCode: args[1], MaterialCode:args[2], Description:args[3], Owner:args[4], PurchaseNumber:purchaseNumber, Price:price, Datetime:args[7]}
	purchaseAsBytes, _ := json.Marshal(purchase)
	APIstub.PutState(args[0], purchaseAsBytes)

	return true
}

// 获取采购记录数目
func getPurchaseNumber(APIstub shim.ChaincodeStubInterface) int {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	return info.PurchaseNumber
}

// 设置采购记录数目
func setPurchaseNumber(APIstub shim.ChaincodeStubInterface, number int)  {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	info.PurchaseNumber = number

	infoAsBytes, _ = json.Marshal(info)
    APIstub.PutState("INFO", infoAsBytes)
}

// 采购
func (s *SmartContract) purchase(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 7 {
		return shim.Error("Incorrect number of arguments. Expecting 7")
	}

	// 1. 添加采购记录
	// 查询采购number从而构建purchaseID
	purchaseNumber := getPurchaseNumber(APIstub)
	purchaseID := "PURCHASE" + strconv.Itoa(purchaseNumber) 

	if !addPurchase(APIstub, []string{purchaseID, args[0], args[1], args[2], args[3], args[4], args[5], args[6]}) {
		return shim.Error("addPurchase error")
	}

	// 修改采购number
	setPurchaseNumber(APIstub, purchaseNumber + 1)

	// 2. 添加物料
	// 查询物料number从而获取materialID
	materialNumber := getMaterialNumber(APIstub)
	materialID := "MATERIAL" + strconv.Itoa(materialNumber)

	newArgs := []string{materialID, args[0], args[1], args[2], args[3], args[4], args[4], args[5], "", "", "", ""} 

	if !createMaterial(APIstub, newArgs) {
		return shim.Error("createMaterial error")
	}

	// 修改物料number
	setMaterialNumber(APIstub, materialNumber + 1)

	return shim.Success(nil)
}

// 查询所有采购记录
func (s *SmartContract) queryAllPurchases(APIstub shim.ChaincodeStubInterface) sc.Response {

	startKey := "PURCHASE0"
	endKey := "PURCHASE999"

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

	fmt.Printf("- queryAllPurchases:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}