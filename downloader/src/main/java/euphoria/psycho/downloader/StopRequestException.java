package euphoria.psycho.downloader;

/**
 * Created by Administrator on 2015/1/6.
 */
public class StopRequestException extends Exception {
    public StopRequestException(String message){
        super(message);
    }
    public StopRequestException(Throwable t){
        super(t.getMessage());
    }
}
