package com.breakinblocks.justenoughcraftingtables.platform;

import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class StorageTransaction implements AutoCloseable {
    private final Transaction transaction = Transaction.openRoot();
    public static StorageTransaction openRoot() { return new StorageTransaction(); }
    public static boolean isOpen() { return Transaction.getLifecycle() != Transaction.Lifecycle.NONE; }
    public void commit() { transaction.commit(); }
    public void close() { transaction.close(); }
}
