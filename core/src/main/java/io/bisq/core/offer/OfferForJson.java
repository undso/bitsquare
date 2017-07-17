package io.bisq.core.offer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.bisq.common.locale.CurrencyUtil;
import io.bisq.common.locale.Res;
import io.bisq.common.monetary.Price;
import io.bisq.common.monetary.Volume;
import io.bisq.common.util.MathUtils;
import io.bisq.core.payment.payload.PaymentMethod;
import org.bitcoinj.core.Coin;
import org.bitcoinj.utils.MonetaryFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class OfferForJson {
    private static final Logger log = LoggerFactory.getLogger(OfferForJson.class);

    public final OfferPayload.Direction direction;
    public final String currencyCode;
    public final long minAmount;
    public final long amount;
    public final long price;
    public final long date;
    public final boolean useMarketBasedPrice;
    public final double marketPriceMargin;
    public final String paymentMethod;
    public final String id;
    public final String offerFeeTxID;

    // primaryMarket fields are based on industry standard where primaryMarket is always in the focus (in the app BTC is always in the focus - will be changed in a larger refactoring once)
    public String currencyPair;
    public OfferPayload.Direction primaryMarketDirection;

    public String priceDisplayString;
    public String primaryMarketAmountDisplayString;
    public String primaryMarketMinAmountDisplayString;
    public String primaryMarketVolumeDisplayString;
    public String primaryMarketMinVolumeDisplayString;

    public long primaryMarketPrice;
    public long primaryMarketAmount;
    public long primaryMarketMinAmount;
    public long primaryMarketVolume;
    public long primaryMarketMinVolume;

    @JsonIgnore
    transient private final MonetaryFormat fiatFormat = new MonetaryFormat().shift(0).minDecimals(4).repeatOptionalDecimals(0, 0);
    @JsonIgnore
    transient private final MonetaryFormat altcoinFormat = new MonetaryFormat().shift(0).minDecimals(8).repeatOptionalDecimals(0, 0);
    @JsonIgnore
    transient private final MonetaryFormat coinFormat = MonetaryFormat.BTC;


    public OfferForJson(OfferPayload.Direction direction,
                        String currencyCode,
                        Coin minAmount,
                        Coin amount,
                        Price price,
                        Date date,
                        String id,
                        boolean useMarketBasedPrice,
                        double marketPriceMargin,
                        PaymentMethod paymentMethod,
                        String offerFeeTxID) {

        this.direction = direction;
        this.currencyCode = currencyCode;
        this.minAmount = minAmount.value;
        this.amount = amount.value;
        this.price = price.getValue();
        this.date = date.getTime();
        this.id = id;
        this.useMarketBasedPrice = useMarketBasedPrice;
        this.marketPriceMargin = marketPriceMargin;
        this.paymentMethod = paymentMethod.getId();
        this.offerFeeTxID = offerFeeTxID;

        setDisplayStrings();
    }

    private void setDisplayStrings() {
        try {
            final Price price = getPrice();
            if (CurrencyUtil.isCryptoCurrency(currencyCode)) {
                primaryMarketDirection = direction == OfferPayload.Direction.BUY ? OfferPayload.Direction.SELL : OfferPayload.Direction.BUY;
                currencyPair = currencyCode + "/" + Res.getBaseCurrencyCode();

                // int precision = 8;
                //decimalFormat.setMaximumFractionDigits(precision);

                // amount and volume is inverted for json
                priceDisplayString = altcoinFormat.noCode().format(price.getMonetary()).toString();
                primaryMarketMinAmountDisplayString = altcoinFormat.noCode().format(getMinVolume().getMonetary()).toString();
                primaryMarketAmountDisplayString = altcoinFormat.noCode().format(getVolume().getMonetary()).toString();
                primaryMarketMinVolumeDisplayString = coinFormat.noCode().format(getMinAmountAsCoin()).toString();
                primaryMarketVolumeDisplayString = coinFormat.noCode().format(getAmountAsCoin()).toString();

                primaryMarketPrice = price.getValue();
                primaryMarketMinAmount = getMinVolume().getValue();
                primaryMarketAmount = getVolume().getValue();
                primaryMarketMinVolume = getMinAmountAsCoin().getValue();
                primaryMarketVolume = getAmountAsCoin().getValue();
            } else {
                primaryMarketDirection = direction;
                currencyPair = Res.getBaseCurrencyCode() + "/" + currencyCode;

                priceDisplayString = fiatFormat.noCode().format(price.getMonetary()).toString();
                primaryMarketMinAmountDisplayString = coinFormat.noCode().format(getMinAmountAsCoin()).toString();
                primaryMarketAmountDisplayString = coinFormat.noCode().format(getAmountAsCoin()).toString();
                primaryMarketMinVolumeDisplayString = fiatFormat.noCode().format(getMinVolume().getMonetary()).toString();
                primaryMarketVolumeDisplayString = fiatFormat.noCode().format(getVolume().getMonetary()).toString();

                // we use precision 4 for fiat based price but on the markets api we use precision 8 so we scale up by 10000
                primaryMarketPrice = (long) MathUtils.scaleUpByPowerOf10(price.getValue(), 4);
                primaryMarketMinVolume = (long) MathUtils.scaleUpByPowerOf10(getMinVolume().getValue(), 4);
                primaryMarketVolume = (long) MathUtils.scaleUpByPowerOf10(getVolume().getValue(), 4);

                primaryMarketMinAmount = getMinAmountAsCoin().getValue();
                primaryMarketAmount = getAmountAsCoin().getValue();
            }

        } catch (Throwable t) {
            log.error("Error at setDisplayStrings: " + t.getMessage());
        }
    }

    private Price getPrice() {
        return Price.valueOf(currencyCode, price);
    }

    private Coin getAmountAsCoin() {
        return Coin.valueOf(amount);
    }

    private Coin getMinAmountAsCoin() {
        return Coin.valueOf(minAmount);
    }

    private Volume getVolume() {
        return getPrice().getVolumeByAmount(getAmountAsCoin());
    }

    private Volume getMinVolume() {
        return getPrice().getVolumeByAmount(getMinAmountAsCoin());
    }
}
