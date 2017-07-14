/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bisq.core.trade;

import io.bisq.common.storage.Storage;
import io.bisq.core.btc.wallet.BtcWalletService;
import io.bisq.core.offer.Offer;
import io.bisq.core.proto.CoreProtoResolver;
import io.bisq.core.trade.protocol.SellerAsTakerProtocol;
import io.bisq.core.trade.protocol.TakerProtocol;
import io.bisq.generated.protobuffer.PB;
import io.bisq.network.p2p.NodeAddress;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public final class SellerAsTakerTrade extends SellerTrade implements TakerTrade {

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor, initialization
    ///////////////////////////////////////////////////////////////////////////////////////////

    public SellerAsTakerTrade(Offer offer,
                              Coin tradeAmount,
                              Coin txFee,
                              Coin takerFee,
                              boolean isCurrencyForTakerFeeBtc,
                              long tradePrice,
                              NodeAddress tradingPeerNodeAddress,
                              Storage<? extends TradableList> storage,
                              BtcWalletService btcWalletService) {
        super(offer, tradeAmount, txFee, takerFee, isCurrencyForTakerFeeBtc, tradePrice,
                tradingPeerNodeAddress, storage, btcWalletService);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public PB.Tradable toProtoMessage() {
        return PB.Tradable.newBuilder()
                .setSellerAsTakerTrade(PB.SellerAsTakerTrade.newBuilder()
                        .setTrade((PB.Trade) super.toProtoMessage()))
                .build();
    }

    public static Tradable fromProto(PB.SellerAsTakerTrade sellerAsTakerTradeProto,
                                     Storage<? extends TradableList> storage,
                                     BtcWalletService btcWalletService,
                                     CoreProtoResolver coreProtoResolver) {
        PB.Trade proto = sellerAsTakerTradeProto.getTrade();
        return Trade.fromProto(new SellerAsTakerTrade(
                        Offer.fromProto(proto.getOffer()),
                        Coin.valueOf(proto.getTradeAmountAsLong()),
                        Coin.valueOf(proto.getTxFeeAsLong()),
                        Coin.valueOf(proto.getTakerFeeAsLong()),
                        proto.getIsCurrencyForTakerFeeBtc(),
                        proto.getTradePrice(),
                        proto.hasTradingPeerNodeAddress() ? NodeAddress.fromProto(proto.getTradingPeerNodeAddress()) : null,
                        storage,
                        btcWalletService),
                proto,
                coreProtoResolver);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void createTradeProtocol() {
        tradeProtocol = new SellerAsTakerProtocol(this);
    }

    @Override
    public void takeAvailableOffer() {
        checkArgument(tradeProtocol instanceof TakerProtocol, "tradeProtocol NOT instanceof TakerProtocol");
        ((TakerProtocol) tradeProtocol).takeAvailableOffer();
    }
}
