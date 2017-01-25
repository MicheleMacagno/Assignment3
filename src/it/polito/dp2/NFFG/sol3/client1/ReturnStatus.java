package it.polito.dp2.NFFG.sol3.client1;

import it.polito.dp2.NFFG.lab3.ServiceException;

/*
 * This class contains constants to identify easily the status code of responses coming from the server
 */
public class ReturnStatus {
	
	public static final int OK=200;
	public static final int OK_CREATED=201;
	public static final int OK_NO_CONTENT=204;
	public static final int BAD_REQUEST = 400;
	public static final int FORBIDDEN=403;
	public static final int NOT_FOUND=404;
	public static final int NOT_ALLOWED=405;
	public static final int INTERNAL_SERVER_ERROR = 500;
	
}
