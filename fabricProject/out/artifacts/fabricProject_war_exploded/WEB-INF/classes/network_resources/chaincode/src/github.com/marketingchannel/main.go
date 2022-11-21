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

	"github.com/hyperledger/fabric/core/chaincode/shim"
	sc "github.com/hyperledger/fabric/protos/peer"
)

// Define the Smart Contract structure
type SmartContract struct {
}

// 信息类，唯一key（"INFO"）
type Info struct {
	CarNumber int `json:"carnumber"`
	SaleNumber int `json:"salenumber"`
}

/*
 * The Init method is called when the Smart Contract "fabcar" is instantiated by the blockchain network
 * Best practice is to have any Ledger initialization in separate function -- see initLedger()
 */
func (s *SmartContract) Init(APIstub shim.ChaincodeStubInterface) sc.Response {
	return shim.Success(nil)
}

/*
 * The Invoke method is called as a result of an application request to run the Smart Contract "fabcar"
 * The calling application program has also specified the particular smart contract function to be called, with arguments
 */
func (s *SmartContract) Invoke(APIstub shim.ChaincodeStubInterface) sc.Response {

	// Retrieve the requested Smart Contract function and arguments
	function, args := APIstub.GetFunctionAndParameters()
	// Route to the appropriate handler function to interact with the ledger appropriately
	if function == "initLedger" {
		return s.initLedger(APIstub)
        } else if function == "queryCar" {
                return s.queryCar(APIstub, args)
        } else if function == "queryAllCars" {
                return s.queryAllCars(APIstub)
	} else if function == "sale" {
		return s.sale(APIstub, args)
	} else if function == "queryAllSales" {
		return s.queryAllSales(APIstub)
	}


	return shim.Error("Invalid Smart Contract function name.")
}

// 初始化账本
func (s *SmartContract) initLedger(APIstub shim.ChaincodeStubInterface) sc.Response {

	// 汽车
	cars := []Car{
                Car{VinCode: "L6T22222222221239", Make: "Org1", OwnerName: "张明", OwnerPhone: "13175961537"},
                Car{VinCode: "L6T22222222220866", Make: "Org2", OwnerName: "王玲", OwnerPhone: "13632973856"},
        }
        i := 0
        for i < len(cars) {
                fmt.Println("i is ", i)
                carAsBytes, _ := json.Marshal(cars[i])
                APIstub.PutState("CAR"+strconv.Itoa(i), carAsBytes)
                fmt.Println("Added", cars[i])
                i = i + 1
        }

        // 汽车索引
        carIndexs := []CarIndex{
        	CarIndex{CarID: "CAR0"},
        	CarIndex{CarID: "CAR1"},
        }
        i = 0
        for i < len(carIndexs) {
                fmt.Println("i is ", i)
                carIndexAsBytes, _ := json.Marshal(carIndexs[i])
                APIstub.PutState(cars[i].VinCode, carIndexAsBytes)
                fmt.Println("Added", carIndexs[i])
                i = i + 1
        }

	// 销售记录
	sales := []Sale{
		Sale{VinCode: "L6T22222222221239", SaleCode: "0000001", ClientCode: "c001", DealerCode: "d001", Price: 54000, OwnerName: "张明", OwnerPhone: "13175961537", Datetime: "2021-2-4"},
		Sale{VinCode: "L6T22222222220866", SaleCode: "0000002", ClientCode: "c002", DealerCode: "d001", Price: 82000, OwnerName: "王玲", OwnerPhone: "13632973856", Datetime: "2021-2-5"},
	}
	i = 0
        for i < len(sales) {
                fmt.Println("i is ", i)
                saleAsBytes, _ := json.Marshal(sales[i])
                APIstub.PutState("SALE"+strconv.Itoa(i), saleAsBytes)
                fmt.Println("Added", sales[i])
                i = i + 1
        }

        // 信息
        info := Info{CarNumber: 2, SaleNumber: 2}
        infoAsBytes, _ := json.Marshal(info)
        APIstub.PutState("INFO", infoAsBytes)
        fmt.Println("Added", info)


	return shim.Success(nil)
}

// The main function is only relevant in unit test mode. Only included here for completeness.
func main() {

	// Create a new Smart Contract
	err := shim.Start(new(SmartContract))
	if err != nil {
		fmt.Printf("Error creating new Smart Contract: %s", err)
	}
}
