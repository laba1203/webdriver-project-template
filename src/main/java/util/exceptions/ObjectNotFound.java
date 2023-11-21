package util.exceptions;

public class ObjectNotFound extends AssertionError {

    public ObjectNotFound(String msg) {
        super(msg);
    }

    public ObjectNotFound() {
        this("Object was not found.");
    }

}
