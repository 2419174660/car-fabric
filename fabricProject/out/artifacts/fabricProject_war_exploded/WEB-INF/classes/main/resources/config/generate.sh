#!/usr/bin/env bash

: "${FABRIC_VERSION:=1.4.1}"
: "${FABRIC_CA_VERSION:=1.4.1}"

#rm -f ./genesis.block
#rm -f ./SupplyChannel.tx
#rm -f ./MarketingChannel.tx
#rm -f ./ServiceChannel.tx

# /usr/local/bin/cryptogen generate --config=./crypto-config.yaml
#####生成创世区块
/usr/local/bin/configtxgen -profile OrdererGenesis -outputBlock genesis.block -channelID syschannel

#####创建通道生成事务
/usr/local/bin/configtxgen -profile SupplyChannel -outputCreateChannelTx generate_channel/SupplyChannel.tx -channelID supplychannel
/usr/local/bin/configtxgen -profile MarketingChannel -outputCreateChannelTx generate_channel/MarketingChannel.tx -channelID marketingchannel
/usr/local/bin/configtxgen -profile ServiceChannel -outputCreateChannelTx generate_channel/ServiceChannel.tx -channelID servicechannel

#######创建锚节点更新文件
/usr/local/bin/configtxgen -profile SupplyChannel -outputAnchorPeersUpdate generate_channel/SupplyChannel_Org1_anchors.tx -channelID supplychannel -asOrg Enterprise01
/usr/local/bin/configtxgen -profile SupplyChannel -outputAnchorPeersUpdate generate_channel/SupplyChannel_Org2_anchors.tx -channelID supplychannel -asOrg Enterprise02
/usr/local/bin/configtxgen -profile SupplyChannel -outputAnchorPeersUpdate generate_channel/SupplyChannel_Org3_anchors.tx -channelID supplychannel -asOrg Enterprise03

/usr/local/bin/configtxgen -profile MarketingChannel -outputAnchorPeersUpdate generate_channel/MarketingChannel_Org3_anchors.tx -channelID marketingchannel -asOrg Enterprise03
/usr/local/bin/configtxgen -profile MarketingChannel -outputAnchorPeersUpdate generate_channel/MarketingChannel_Org4_anchors.tx -channelID marketingchannel -asOrg Enterprise04

/usr/local/bin/configtxgen -profile ServiceChannel -outputAnchorPeersUpdate generate_channel/ServiceChannel_Org4_anchors.tx -channelID servicechannel -asOrg Enterprise04
/usr/local/bin/configtxgen -profile ServiceChannel -outputAnchorPeersUpdate generate_channel/ServiceChannel_Org5_anchors.tx -channelID servicechannel -asOrg Enterprise05
