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

// 销售类
type Sale struct {
	VinCode string `json:"vincode"`
	SaleCode string `json:"salecode"`
	ClientCode string `json:"clientcode"`
	DealerCode string `json:"dealercode"`
	Price float64 `json:"price"`
	OwnerName string `json:"ownername"`
    OwnerPhone string `json:"ownerphone"`
    Datetime string `json:"datetime"`
}

// 添加销售记录
func addSale(APIstub shim.ChaincodeStubInterface, args []string) bool {

	if len(args) != 9 {
		fmt.Println("Incorrect number of arguments. Expecting 9")
		return false
	}

	// 添加
	price, err := strconv.ParseFloat(args[5], 64)
	if err != nil {
		fmt.Println(err.Error())
		return false
	}

	var sale = Sale{VinCode:args[1], SaleCode:args[2], ClientCode:args[3], DealerCode:args[4], Price:price, OwnerName:args[6], OwnerPhone:args[7], Datetime:args[8]}
	saleAsBytes, _ := json.Marshal(sale)
	APIstub.PutState(args[0], saleAsBytes)

	return true
}

// 获取销售记录数目
func getSaleNumber(APIstub shim.ChaincodeStubInterface) int {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	return info.SaleNumber
}

// 设置销售记录数目
func setSaleNumber(APIstub shim.ChaincodeStubInterface, number int)  {
	infoAsBytes, _ := APIstub.GetState("INFO")
	info := Info{}

	json.Unmarshal(infoAsBytes, &info)
	info.SaleNumber = number

	infoAsBytes, _ = json.Marshal(info)
    APIstub.PutState("INFO", infoAsBytes)
}

// 销售
func (s *SmartContract) sale(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 8 {
		return shim.Error("Incorrect number of arguments. Expecting 8")
	}

	// 1. 添加销售记录
	// 查询销售number从而构建saleID
	saleNumber := getSaleNumber(APIstub)
	saleID := "SALE" + strconv.Itoa(saleNumber) 

	if !addSale(APIstub, []string{saleID, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]}) {
		return shim.Error("addSale error")
	}

	// 修改结算number
	setSaleNumber(APIstub, saleNumber + 1)

	// 2. 更新汽车信息
	// 先查询carID
	carID := getCarID(APIstub, []string{args[0]})
	if carID == "" {
		return shim.Error("getCarID error")
	}

	// 更新汽车车主信息
	if !changeCarOwner(APIstub, []string{carID, args[5], args[6]}) {
		return shim.Error("changeCarOwner error")
	}

	return shim.Success(nil)
}

// 查询所有销售记录
func (s *SmartContract) queryAllSales(APIstub shim.ChaincodeStubInterface) sc.Response {

	startKey := "SALE0"
	endKey := "SALE999"

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

	fmt.Printf("- queryAllSales:\n%s\n", buffer.String())

	return shim.Success(buffer.Bytes())
}