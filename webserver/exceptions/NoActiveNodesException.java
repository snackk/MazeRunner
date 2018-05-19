package  webserver.exceptions;

public class NoActiveNodesException extends MazeRunnerException {

	private static final long serialVersionUID = -1557912935765275684L;

	public NoActiveNodesException() {
		super("There are no active nodes. Please wait for one to start.");
	}
}
