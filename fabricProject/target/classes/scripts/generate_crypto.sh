#!/usr/bin/env bash

: "${FABRIC_VERSION:=1.4.1}"
: "${FABRIC_CA_VERSION:=1.4.1}"

#rm -f ./genesis.block
#rm -f ./SupplyChannel.tx
#rm -f ./MarketingChannel.tx
#rm -f ./ServiceChannel.tx

#生成crypto-config文件
/usr/local/bin/cryptogen generate --config=../config/crypto-config.yaml --output ../network_resources/crypto-config
