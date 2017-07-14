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

package io.bisq.provider.price;

import lombok.Value;

@SuppressWarnings("FieldCanBeLocal")
@Value
public class PriceData {
    private final String currencyCode;
    private final double price;
    private final long timestampSec;

    public PriceData(String currencyCode, double price, long timestampSec) {
        this.currencyCode = currencyCode;
        this.price = price;
        this.timestampSec = timestampSec;
    }
}
