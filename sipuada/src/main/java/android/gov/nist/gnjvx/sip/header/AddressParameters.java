package android.gov.nist.gnjvx.sip.header;

import java.util.Map;
import java.util.Map.Entry;

import android.javax.sip.address.Address;
import android.javax.sip.header.Parameters;

public interface AddressParameters extends Parameters {

	/**
	 * get the Address field
	 * @return the imbedded  Address
	 */
	public abstract Address getAddress();

	/**
	 * set the Address field
	 * @param address Address to set
	 */
	public abstract void setAddress(Address address);
	
	/**
	 * Get the parameters map.
	 * 
	 */
	public abstract Map<String,Entry<String,String>> getParameters();


}