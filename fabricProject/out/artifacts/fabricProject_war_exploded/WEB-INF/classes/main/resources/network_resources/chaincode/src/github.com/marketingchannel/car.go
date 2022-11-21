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

	"github.com/hyperledger/fabric/core/chaincode/shim"
	sc "github.com/hyperledger/fabric/protos/peer"
)

// 汽车类
type Car struct {
        VinCode string `json:"vincode"`
        Make string `json:"make"`
        OwnerName string `json:"ownername"`
        OwnerPhone string `json:"ownerphone"`
}

// 汽车索引类
type CarIndex struct {
        CarID string `json:"carid"`
}

func (s *SmartContract) Init(APIstub shim.ChaincodeStubInterface) sc.Response {
	return shim.Success(nil)
}

// 根据carID查询汽车
func (s *SmartContract) queryCar(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	carAsBytes, _ := APIstub.GetState(args[0])
	return shim.Success(carAsBytes)
}

// 添加汽车
func createCar(APIstub shim.ChaincodeStubInterface, args []string) bool {

        if len(args) != 5 {
                fmt.Println("Incorrect number of arguments. Expecting 5")
                return false
        }

        // 添加汽车和汽车索引
        var car = Car{VinCode: args[1], Make: args[2], OwnerName: args[3], OwnerPhone: args[4]}
        var carIndex = CarIndex{CarID: args[0]}

        carAsBytes, _ := json.Marshal(car)
        carIndexAsBytes, _ := json.Marshal(carIndex)
        APIstub.PutState(args[0], carAsBytes)
        APIstub.PutState(args[1], carIndexAsBytes)

        return true
}

// 查询所有汽车
func (s *SmartContract) queryAllCars(APIstub shim.ChaincodeStubInterface) sc.Response {

        startKey := "CAR0"
        endKey := "CAR999"

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

        fmt.Printf("- queryAllCars:\n%s\n", buffer.String())

        return shim.Success(buffer.Bytes())
}

// 修改汽车车主信息
func changeCarOwner(APIstub shim.ChaincodeStubInterface, args []string) bool {

	if len(args) != 3 {
                fmt.Println("Incorrect number of arguments. Expecting 3")
                return false
        }

        carAsBytes, _ := APIstub.GetState(args[0])
        car := Car{}

        json.Unmarshal(carAsBytes, &car)
        car.OwnerName = args[1]
        car.OwnerPhone = args[2]

        carAsBytes, _ = json.Marshal(car)
        APIstub.PutState(args[0], carAsBytes)

        return true
}

// 获取carID
func getCarID(APIstub shim.ChaincodeStubInterface, args []string) string {

        if len(args) != 1 {
                fmt.Println("Incorrect number of arguments. Expecting 1")
                return ""
        }
        carIndexAsBytes, _ := APIstub.GetState(args[0])
        carIndex := CarIndex{}

        json.Unmarshal(carIndexAsBytes, &carIndex)

        return carIndex.CarID
}

// 获取汽车记录数目
func getCarNumber(APIstub shim.ChaincodeStubInterface) int  {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	return info.CarNumber
}

// 设置汽车记录数目
func setCarNumber(APIstub shim.ChaincodeStubInterface, number int)  {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	info.CarNumber = number

	infoAsBytes, _ = json.Marshal(info)
        APIstub.PutState("INFO", infoAsBytes)
}
