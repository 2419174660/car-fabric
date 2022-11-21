#!/usr/bin/env bash

: "${FABRIC_VERSION:=1.4.1}"
: "${FABRIC_CA_VERSION:=1.4.1}"

#rm -f ./genesis.block
#rm -f ./SupplyChannel.tx
#rm -f ./MarketingChannel.tx
#rm -f ./ServiceChannel.tx

#####生成创世区块
/usr/local/bin/configtxgen -profile OrdererGenesis -outputBlock ../network_resources/genesis.block -channelID syschannel

#####创建通道生成事务
######三条供应链
/usr/local/bin/configtxgen -profile SupplyChannelOne -outputCreateChannelTx ../network_resources/generate_channel_tx/SupplyChannelOne.tx -channelID supplychannelone
/usr/local/bin/configtxgen -profile SupplyChannelTwo -outputCreateChannelTx ../network_resources/generate_channel_tx/SupplyChannelTwo.tx -channelID supplychanneltwo
/usr/local/bin/configtxgen -profile SupplyChannelThree -outputCreateChannelTx ../network_resources/generate_channel_tx/SupplyChannelThree.tx -channelID supplychannelthree
######营销链
/usr/local/bin/configtxgen -profile MarketingChannel -outputCreateChannelTx ../network_resources/generate_channel_tx/MarketingChannel.tx -channelID marketingchannel
#####服务链
/usr/local/bin/configtxgen -profile ServiceChannel -outputCreateChannelTx ../network_resources/generate_channel_tx/ServiceChannel.tx -channelID servicechannel

#######创建锚节点更新文件
#/usr/local/bin/configtxgen -profile SupplyChannel -outputAnchorPeersUpdate ../network_resources/generate_channel_tx/SupplyChannel_Org1_anchors.tx -channelID supplychannel -asOrg Enterprise01
#/usr/local/bin/configtxgen -profile SupplyChannel -outputAnchorPeersUpdate ../network_resources/generate_channel_tx/SupplyChannel_Org2_anchors.tx -channelID supplychannel -asOrg Enterprise02
#/usr/local/bin/configtxgen -profile SupplyChannel -outputAnchorPeersUpdate ../network_resources/generate_channel_tx/SupplyChannel_Org3_anchors.tx -channelID supplychannel -asOrg Enterprise03

#/usr/local/bin/configtxgen -profile MarketingChannel -outputAnchorPeersUpdate ../network_resources/generate_channel_tx/MarketingChannel_Org3_anchors.tx -channelID marketingchannel -asOrg Enterprise03
#/usr/local/bin/configtxgen -profile MarketingChannel -outputAnchorPeersUpdate ../network_resources/generate_channel_tx/MarketingChannel_Org4_anchors.tx -channelID marketingchannel -asOrg Enterprise04

#/usr/local/bin/configtxgen -profile ServiceChannel -outputAnchorPeersUpdate ../network_resources/generate_channel_tx/ServiceChannel_Org4_anchors.tx -channelID servicechannel -asOrg Enterprise04
#/usr/local/bin/configtxgen -profile ServiceChannel -outputAnchorPeersUpdate ../network_resources/generate_channel_tx/ServiceChannel_Org5_anchors.tx -channelID servicechannel -asOrg Enterprise05
