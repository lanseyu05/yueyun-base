package online.yueyun.storage.exception;

/**
 * 存储异常类
 * 
 * @author yueyun
 */
public class StorageException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
} 