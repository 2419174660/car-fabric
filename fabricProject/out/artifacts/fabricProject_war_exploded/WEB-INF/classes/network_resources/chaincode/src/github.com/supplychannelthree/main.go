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
	MaterialNumber int `json:"materialnumber"`
	CarNumber int `json:"carnumber"`
	PurchaseNumber int `json:"purchasenumber"`
	WarehouseNumber int `json:"warehousenumber"`
	MakingNumber int `json:"makingnumber"`
	SettlementNumber int `json:"settlementnumber"`
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
	if function == "queryMaterial" {
		return s.queryMaterial(APIstub, args)
	} else if function == "initLedger" {
		return s.initLedger(APIstub)
	} else if function == "queryAllMaterials" {
		return s.queryAllMaterials(APIstub)
        } else if function == "queryCar" {
                return s.queryCar(APIstub, args)
        } else if function == "queryAllCars" {
                return s.queryAllCars(APIstub)
        } else if function == "purchase" {
		return s.purchase(APIstub, args)
	} else if function == "warehouse" {
		return s.warehouse(APIstub, args)
	} else if function == "making" {
		return s.making(APIstub, args)
	} else if function == "settlement" {
		return s.settlement(APIstub, args)
	} else if function == "queryAllPurchases" {
		return s.queryAllPurchases(APIstub)
	} else if function == "queryAllWarehouses" {
		return s.queryAllWarehouses(APIstub)
	} else if function == "queryAllMakings" {
		return s.queryAllMakings(APIstub)
	} else if function == "queryAllSettlements" {
		return s.queryAllSettlements(APIstub)
	} else if function == "getCarID" {
      	    return s.getCarID(APIstub)
      	} else if function == "changeCarOwner" {
      	    return s.changeCarOwner(APIstub)
      	} else if function == "getMaterialID" {
      	    return s.getMaterialID(APIstub)
      	}


	return shim.Error("Invalid Smart Contract function name.")
}

// 初始化账本
func (s *SmartContract) initLedger(APIstub shim.ChaincodeStubInterface) sc.Response {
	// 物料
	materials := []Material{
		Material{PurchaseCode: "450011111703", MaterialCode: "401111113CDB", Description: "模具垫块合金材质100x150x1900", Owner:"Org1", PurchaseNumber:100, CurrentNumber:80, Price:306.14, SettlementMethod:"承兑汇票", SettlementDatetime:"2021-1-31", WarehouseReservoir:"AS1-TT1", WarehousePosition:"AS1-TT1-TA2"},
		Material{PurchaseCode: "450011111704", MaterialCode: "400111114CDB", Description: "D型卸扣T8级3.25T", Owner:"Org1", PurchaseNumber:70, CurrentNumber:60, Price:2425.32, SettlementMethod:"承兑汇票", SettlementDatetime:"2021-1-31", WarehouseReservoir:"AS2SL", WarehousePosition:"TB22"},
	}
	i := 0
	for i < len(materials) {
		fmt.Println("i is ", i)
		materialAsBytes, _ := json.Marshal(materials[i])
		APIstub.PutState("MATERIAL"+strconv.Itoa(i), materialAsBytes)
		fmt.Println("Added", materials[i])
		i = i + 1
	}

	// 汽车
	cars := []Car{
                Car{VinCode: "L6T22222222221239", Make: "Org1", OwnerName: "张明", OwnerPhone: "13175961537"},
                Car{VinCode: "L6T22222222220866", Make: "Org2", OwnerName: "王玲", OwnerPhone: "13632973856"},
        }
        i = 0
        for i < len(cars) {
                fmt.Println("i is ", i)
                carAsBytes, _ := json.Marshal(cars[i])
                APIstub.PutState("CAR"+strconv.Itoa(i), carAsBytes)
                fmt.Println("Added", cars[i])
                i = i + 1
        }

        // 物料索引
        materialIndexs := []MaterialIndex{
        	MaterialIndex{MaterialID: "MATERIAL0"},
        	MaterialIndex{MaterialID: "MATERIAL1"},
        }
        i = 0
        for i < len(materialIndexs) {
                fmt.Println("i is ", i)
                materialIndexAsBytes, _ := json.Marshal(materialIndexs[i])
                APIstub.PutState(materials[i].PurchaseCode + "#" + materials[i].MaterialCode, materialIndexAsBytes)
                fmt.Println("Added", materialIndexs[i])
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

        // 采购记录
        purchases := []Purchase{
		Purchase{PurchaseCode: "450011111703", MaterialCode: "401111113CDB", Description: "模具垫块合金材质100x150x1900", Owner:"Org1", PurchaseNumber:100, Price:306.14, Datetime:"2021-1-31"},
		Purchase{PurchaseCode: "450011111704", MaterialCode: "400111114CDB", Description: "D型卸扣T8级3.25T", Owner:"Org1", PurchaseNumber:70, Price:2425.32, Datetime:"2021-1-31"},
	}
	i = 0
        for i < len(purchases) {
                fmt.Println("i is ", i)
                purchaseAsBytes, _ := json.Marshal(purchases[i])
                APIstub.PutState("PURCHASE"+strconv.Itoa(i), purchaseAsBytes)
                fmt.Println("Added", purchases[i])
                i = i + 1
        }

	// 仓储记录
	warehouses := []Warehouse{
		Warehouse{PurchaseCode: "450011111703", MaterialCode: "401111113CDB", WarehouseReservoir:"AS1-TT1", WarehousePosition:"AS1-TT1-TA2", Datetime:"2021-2-1"},
		Warehouse{PurchaseCode: "450011111704", MaterialCode: "400111114CDB", WarehouseReservoir:"AS2SL", WarehousePosition:"TB22", Datetime:"2021-2-2"},
	}
	i = 0
        for i < len(warehouses) {
                fmt.Println("i is ", i)
                warehouseAsBytes, _ := json.Marshal(warehouses[i])
                APIstub.PutState("WAREHOUSE"+strconv.Itoa(i), warehouseAsBytes)
                fmt.Println("Added", warehouses[i])
                i = i + 1
        }

	// 制造记录
	makings := []Making{
		Making{PurchaseCode: "450011111703", MaterialCode: "401111113CDB", CostNumber: 20, VinCode: "L6T22222222221239", Make: "Org1", Datetime: "2021-2-4"},
		Making{PurchaseCode: "450011111704", MaterialCode: "400111114CDB", CostNumber: 10, VinCode: "L6T22222222220866", Make: "Org2", Datetime: "2021-2-5"},
	}
	i = 0
        for i < len(makings) {
                fmt.Println("i is ", i)
                makingAsBytes, _ := json.Marshal(makings[i])
                APIstub.PutState("MAKING"+strconv.Itoa(i), makingAsBytes)
                fmt.Println("Added", makings[i])
                i = i + 1
        }

	// 结算记录
	settlements := []Settlement{
		Settlement{PurchaseCode: "450011111703", MaterialCode: "401111113CDB", SettlementMethod:"承兑汇票", SettlementDatetime:"2021-2-15"},
		Settlement{PurchaseCode: "450011111704", MaterialCode: "400111114CDB", SettlementMethod:"承兑汇票", SettlementDatetime:"2021-2-16"},
	}
	i = 0
        for i < len(settlements) {
                fmt.Println("i is ", i)
                settlementAsBytes, _ := json.Marshal(settlements[i])
                APIstub.PutState("SETTLEMENT"+strconv.Itoa(i), settlementAsBytes)
                fmt.Println("Added", settlements[i])
                i = i + 1
        }

        // 信息
        info := Info{MaterialNumber: 2, CarNumber: 2, PurchaseNumber: 2, WarehouseNumber: 2, MakingNumber: 2, SettlementNumber: 2}
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
