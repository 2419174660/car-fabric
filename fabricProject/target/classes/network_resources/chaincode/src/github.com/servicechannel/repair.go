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

// 维修类
type Repair struct {
	VinCode string `json:"vincode"`
	TrustCode string `json:"trustcode"`
	ServiceProviderCode string `json:"serviceprovidercode"`
	LicensePlate string `json:"licenseplate"`
	Datetime string `json:"datetime"`
}

// 添加维修记录
func addRepair(APIstub shim.ChaincodeStubInterface, args []string) bool {

	if len(args) != 6 {
		fmt.Println("Incorrect number of arguments. Expecting 6")
		return false
	}

	// 添加
	var repair = Repair{VinCode:args[1], TrustCode:args[2], ServiceProviderCode:args[3], LicensePlate:args[4], Datetime:args[5]}
	repairAsBytes, _ := json.Marshal(repair)
	APIstub.PutState(args[0], repairAsBytes)

	return true
}

// 获取维修记录数目
func getRepairNumber(APIstub shim.ChaincodeStubInterface) int {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	return info.RepairNumber
}

// 设置维修记录数目
func setRepairNumber(APIstub shim.ChaincodeStubInterface, number int)  {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	info.RepairNumber = number

	infoAsBytes, _ = json.Marshal(info)
    APIstub.PutState("INFO", infoAsBytes)
}

// 维修
func (s *SmartContract) repair(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {
	
	if len(args) != 5 {
		return shim.Error("Incorrect number of arguments. Expecting 5")
	}

	// 查询维修number从而构建repairID
	repairNumber := getRepairNumber(APIstub)
	repairID := "REPAIR" + strconv.Itoa(repairNumber)

	if !addRepair(APIstub, []string{repairID, args[0], args[1], args[2], args[3], args[4]}) {
		return shim.Error("addRepair error")
	}

	// 修改维修number
	setRepairNumber(APIstub, repairNumber + 1)

	return shim.Success(nil)
}

// 查询所有维修记录
func (s *SmartContract) queryAllRepairs(APIstub shim.ChaincodeStubInterface) sc.Response {

	startKey := "REPAIR0"
	endKey := "REPAIR999"

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

	fmt.Printf("- queryAllRepairs:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}