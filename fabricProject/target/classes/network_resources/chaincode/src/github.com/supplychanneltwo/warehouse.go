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

// 仓储类
type Warehouse struct {
	PurchaseCode  string `json:"purchasecode"`
	MaterialCode string `json:"materialcode"`
	WarehouseReservoir string `json:"warehousereservoir"`
    WarehousePosition string `json:"warehouseposition"`
    Datetime string `json:"datetime"`
}

// 添加仓储记录
func addWarehouse(APIstub shim.ChaincodeStubInterface, args []string) bool {

	if len(args) != 6 {
		fmt.Println("Incorrect number of arguments. Expecting 6")
		return false
	}

	// 添加
	var warehouse = Warehouse{PurchaseCode:args[1], MaterialCode:args[2], WarehouseReservoir:args[3], WarehousePosition:args[4], Datetime:args[5]}
	warehouseAsBytes, _ := json.Marshal(warehouse)
	APIstub.PutState(args[0], warehouseAsBytes)

	return true
}

// 获取仓储记录数目
func getWarehouseNumber(APIstub shim.ChaincodeStubInterface) int {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	return info.WarehouseNumber
}

// 设置仓储记录数目
func setWarehouseNumber(APIstub shim.ChaincodeStubInterface, number int)  {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	info.WarehouseNumber = number

	infoAsBytes, _ = json.Marshal(info)
    APIstub.PutState("INFO", infoAsBytes)
}

// 仓储
func (s *SmartContract) warehouse(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 5 {
		return shim.Error("Incorrect number of arguments. Expecting 5")
	}

	// 1. 添加仓储记录
	// 查询仓储number从而构建warehouseID
	warehouseNumber := getWarehouseNumber(APIstub)
	warehouseID := "WAREHOUSE" + strconv.Itoa(warehouseNumber)

	if !addWarehouse(APIstub, []string{warehouseID, args[0], args[1], args[2], args[3], args[4]}) {
		return shim.Error("addWarehouse error")
	}

	// 修改仓储number
	setWarehouseNumber(APIstub, warehouseNumber + 1)

	// 2.修改物料的仓储信息
	// 先查询materialID
	materialID := getMaterialID(APIstub, []string{args[0], args[1]})
	if materialID == "" {
		return shim.Error("getMaterialID error")
	}

	// 再根据MaterialID更改物料的仓储信息
	if !changeMaterialWarehouse(APIstub, []string{materialID, args[2], args[3]}) {
		return shim.Error("changeMaterialWarehouse error")
	}

	return shim.Success(nil)
}

// 查询所有仓储记录
func (s *SmartContract) queryAllWarehouses(APIstub shim.ChaincodeStubInterface) sc.Response {

	startKey := "WAREHOUSE0"
	endKey := "WAREHOUSE999"

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

	fmt.Printf("- queryAllWarehouses:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}