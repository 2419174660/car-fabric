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


// 制造类
type Making struct {
	PurchaseCode  string `json:"purchasecode"`
	MaterialCode string `json:"materialcode"`
	CostNumber int `json:"costnumber"`
	VinCode string `json:"vincode"`
	Make string `json:"make"`
	Datetime string `json:"datetime"`	
}

// 添加制造记录
func addMaking(APIstub shim.ChaincodeStubInterface, args []string) bool {

	if len(args) != 7 {
		fmt.Println("Incorrect number of arguments. Expecting 7")
		return false
	}

	// 添加
	costNumber, err := strconv.Atoi(args[3])
	if err != nil {
		fmt.Println(err.Error())
		return false
	}

	var making = Making{PurchaseCode:args[1], MaterialCode:args[2], CostNumber:costNumber, VinCode:args[4], Make:args[5], Datetime:args[6]}
	makingAsBytes, _ := json.Marshal(making)
	APIstub.PutState(args[0], makingAsBytes)

	return true
}

// 获取制造记录数目
func getMakingNumber(APIstub shim.ChaincodeStubInterface) int {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	return info.MakingNumber
}

// 设置制造记录数目
func setMakingNumber(APIstub shim.ChaincodeStubInterface, number int)  {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	info.MakingNumber = number

	infoAsBytes, _ = json.Marshal(info)
    APIstub.PutState("INFO", infoAsBytes)
}

// 制造
func (s *SmartContract) making(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 6 {
		return shim.Error("Incorrect number of arguments. Expecting 6")
	}

	// 1. 添加制造记录
	// 查询制造number从而构建makingID
	makingNumber := getMakingNumber(APIstub)
	makingID := "MAKING" + strconv.Itoa(makingNumber) 

	if !addMaking(APIstub, []string{makingID, args[0], args[1], args[2], args[3], args[4], args[5]}) {
		return shim.Error("addMaking error")
	}

	// 修改制造number
	setMakingNumber(APIstub, makingNumber + 1)

	// 2. 消耗物料
	// 先查询materialID
	materialID := getMaterialID(APIstub, []string{args[0], args[1]})
	if materialID == "" {
		return shim.Error("getMaterialID error")
	} 

	// 判断并修改物料数目
	if !changeMaterialCurrentNumber(APIstub, []string{materialID, args[2]}) {
		return shim.Error("changeMaterialCurrentNumber error")
	}

	// 3. 添加汽车
	// 新增汽车
	// 查询汽车number从而获取carID
	carNumber := getCarNumber(APIstub)
	carID := "CAR" + strconv.Itoa(carNumber)
	if !createCar(APIstub, []string{carID, args[3], args[4], "", ""}) {
		return shim.Error("createCar error")
	}

	// 修改汽车number
	setCarNumber(APIstub, carNumber + 1)

	return shim.Success(nil)
}

// 查询所有制造记录
func (s *SmartContract) queryAllMakings(APIstub shim.ChaincodeStubInterface) sc.Response {

	startKey := "MAKING0"
	endKey := "MAKING999"

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

	fmt.Printf("- queryAllMakings:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}