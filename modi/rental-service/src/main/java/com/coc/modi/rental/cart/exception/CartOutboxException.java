package com.coc.modi.rental.cart.exception;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.rental.rental.exception.RentalException;

public class CartOutboxException extends RentalException {

    public CartOutboxException(String message) {
        super(ErrorCode.INTERNAL_ERROR, message);
    }
}
