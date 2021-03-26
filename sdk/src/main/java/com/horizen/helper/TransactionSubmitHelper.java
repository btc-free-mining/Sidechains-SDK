package com.horizen.helper;

import com.horizen.box.Box;
import com.horizen.proposition.Proposition;
import com.horizen.transaction.BoxTransaction;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface TransactionSubmitHelper {

    public void submitTransaction(BoxTransaction<Proposition, Box<Proposition>> tx, BiConsumer<Boolean, Optional<Throwable>> callback);

}
