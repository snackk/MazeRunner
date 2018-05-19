package webserver.exceptions;

public class MazeRunnerException extends RuntimeException {

    private String message;

    public MazeRunnerException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
