package webserver.exceptions;

public class NotEnoughNodesException extends MazeRunnerException {

	private static final long serialVersionUID = -1526342935765275684L;

	public NotEnoughNodesException() {
		super("There aren't enough Nodes to process your request.");
	}
}
