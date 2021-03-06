package io.chengguo.streaming.rtsp.header;

import androidx.annotation.NonNull;

import io.chengguo.streaming.utils.Utils;

/**
 * 值为整数的Header
 * Created by fingerart on 2018-07-14.
 */
public class IntegerHeader extends Header<Integer> {

    public IntegerHeader(String name, Integer value) {
        super(name, value);
    }

    public IntegerHeader(@NonNull String nameOrRawHeader) {
        super(nameOrRawHeader);
    }

    @Override
    protected Integer parseValue(String value) {
        try {
            return Integer.parseInt(Utils.trimSafely(value));
        } catch (Exception ignored) {
        }
        return 0;
    }
}