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
	"bytes"
	"encoding/json"
	"fmt"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	sc "github.com/hyperledger/fabric/protos/peer"
)

// Define the material and car structure, with 11 and 4 properties.  Structure tags are used by encoding/json library
// 物料类
type Material struct {
	PurchaseCode  string `json:"purchasecode"`
	MaterialCode string `json:"materialcode"`
	Description  string `json:"description"`
	Owner  string `json:"owner"`
        PurchaseNumber int `json:"purchasenumber"`
        CurrentNumber int `json:"currentnumber"`
        Price float64 `json:"price"`
        SettlementMethod string `json:"settlementmethod"`
        SettlementDatetime string `json:"settlementdatatime"`
        WarehouseReservoir string `json:"warehousereservoir"`
        WarehousePosition string `json:"warehouseposition"`
}

// 物料索引类
type MaterialIndex struct {
	MaterialID string `json:"materialid"`
}

// 根据materialID查询物料
func (s *SmartContract) queryMaterial(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	materialAsBytes, _ := APIstub.GetState(args[0])
	return shim.Success(materialAsBytes)
}

// 添加物料
func createMaterial(APIstub shim.ChaincodeStubInterface, args []string) bool {

	if len(args) != 12 {
		fmt.Println("Incorrect number of arguments. Expecting 12")
		return false
	}

	// 添加物料和物料索引
	purchaseNumber, err1 := strconv.Atoi(args[5])
	if err1 != nil {
		fmt.Println(err1.Error())
		return false
	}
	currentNumber, err2 := strconv.Atoi(args[6])
	if err2 != nil {
		fmt.Println(err2.Error())
		return false
	}
	price, err3 := strconv.ParseFloat(args[7], 64)
	if err3 != nil {
		fmt.Println(err3.Error())
		return false
	}

	var material = Material{PurchaseCode: args[1], MaterialCode: args[2], Description: args[3], Owner:args[4], PurchaseNumber:purchaseNumber, CurrentNumber:currentNumber, Price:price, SettlementMethod:args[8], SettlementDatetime:args[9], WarehouseReservoir:args[10], WarehousePosition:args[11]}
	var materialIndex = MaterialIndex{MaterialID: args[0]}

	materialAsBytes, _ := json.Marshal(material)
	materialIndexAsBytes, _ := json.Marshal(materialIndex)
	APIstub.PutState(args[0], materialAsBytes)
	APIstub.PutState(args[1] + "#" + args[2], materialIndexAsBytes)

	return true
}

// 查询所有物料
func (s *SmartContract) queryAllMaterials(APIstub shim.ChaincodeStubInterface) sc.Response {

	startKey := "MATERIAL0"
	endKey := "MATERIAL999"

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

	fmt.Printf("- queryAllMaterials:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}

// 修改物料仓储信息
func changeMaterialWarehouse(APIstub shim.ChaincodeStubInterface, args []string) bool {

	if len(args) != 3 {
		fmt.Println("Incorrect number of arguments. Expecting 3")
		return false
	}

	materialAsBytes, _ := APIstub.GetState(args[0])
	material := Material{}

	json.Unmarshal(materialAsBytes, &material)
	material.WarehouseReservoir = args[1]
	material.WarehousePosition = args[2]

	materialAsBytes, _ = json.Marshal(material)
	APIstub.PutState(args[0], materialAsBytes)

	return true
}

// 修改物料结算信息
func changeMaterialSettlement(APIstub shim.ChaincodeStubInterface, args []string) bool {

        if len(args) != 3 {
                fmt.Println("Incorrect number of arguments. Expecting 3")
                return false
        }

        materialAsBytes, _ := APIstub.GetState(args[0])
        material := Material{}

        json.Unmarshal(materialAsBytes, &material)
        material.SettlementMethod = args[1]
        material.SettlementDatetime = args[2]

        materialAsBytes, _ = json.Marshal(material)
        APIstub.PutState(args[0], materialAsBytes)

        return true
}

// 修改物料当前数量
func changeMaterialCurrentNumber(APIstub shim.ChaincodeStubInterface, args []string) bool {

        if len(args) != 2 {
                fmt.Println("Incorrect number of arguments. Expecting 2")
                return false
        }

        materialAsBytes, _ := APIstub.GetState(args[0])
        material := Material{}

        json.Unmarshal(materialAsBytes, &material)
        
        // 减去消耗的物料数目
        costNumber, err := strconv.Atoi(args[1])
        if err != nil {
        	fmt.Println(err.Error())
        	return false
        }

        if material.CurrentNumber < costNumber {
        	fmt.Println("costNumber bigger than currentNumber")
        	return false
        }
        material.CurrentNumber -= costNumber

        materialAsBytes, _ = json.Marshal(material)
        APIstub.PutState(args[0], materialAsBytes)

        return true
}

// 获取materialID
func getMaterialID(APIstub shim.ChaincodeStubInterface, args []string) string {

	if len(args) != 2 {
                fmt.Println("Incorrect number of arguments. Expecting 2")
                return ""
        }
	materialIndexAsBytes, _ := APIstub.GetState(args[0] + "#" + args[1])
	materialIndex := MaterialIndex{}

        json.Unmarshal(materialIndexAsBytes, &materialIndex)

        return materialIndex.MaterialID
}

// 获取物料记录数目
func getMaterialNumber(APIstub shim.ChaincodeStubInterface) int {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	return info.MaterialNumber
}

// 设置物料记录数目
func setMaterialNumber(APIstub shim.ChaincodeStubInterface, number int)  {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	info.MaterialNumber = number

	infoAsBytes, _ = json.Marshal(info)
        APIstub.PutState("INFO", infoAsBytes)
}