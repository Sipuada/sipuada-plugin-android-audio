/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package android.gov.nist.gnjvx.sip.header;

import java.text.*;

/**
 * Content Dispositon SIP Header.
 * 
 * @author M. Ranganathan   <br/>
 * @version 1.2 $Revision: 1.4 $ $Date: 2006/07/13 09:01:06 $
 * @since 1.1
 *
 */
public final class ContentDisposition
	extends ParametersHeader
	implements android.javax.sip.header.ContentDispositionHeader {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 835596496276127003L;
	/**
	 * DispositionType field.  
	 */
	protected String dispositionType;

	/**
	 * Default constructor.
	 */
	public ContentDisposition() {
		super(NAME);
	}

	/**
	 * Encode value of header into canonical string.
	 * @return encoded value of header.
	 *
	 */
	public String encodeBody() {
		StringBuffer encoding = new StringBuffer(dispositionType);
		if (!this.parameters.isEmpty()) {
			encoding.append(SEMICOLON).append(parameters.encode());
		}
		return encoding.toString();
	}

	/**
	 * Set the disposition type.
	 * @param dispositionType type.
	 */
	public void setDispositionType(String dispositionType)
		throws ParseException {
		if (dispositionType == null)
			throw new NullPointerException(
				"JAIN-SIP Exception"
					+ ", ContentDisposition, setDispositionType(), the dispositionType parameter is null");
		this.dispositionType = dispositionType;
	}

	/**
	 * Get the disposition type.
	 * @return Disposition Type
	 */
	public String getDispositionType() {
		return this.dispositionType;
	}

	/**
	 * Get the dispositionType field.
	 * @return String
	 */
	public String getHandling() {
		return this.getParameter("handling");
	}

	/** set the dispositionType field.
	 * @param handling String to set.
	 */
	public void setHandling(String handling) throws ParseException {
		if (handling == null)
			throw new NullPointerException(
				"JAIN-SIP Exception"
					+ ", ContentDisposition, setHandling(), the handling parameter is null");
		this.setParameter("handling", handling);
	}

	/**
	 * Gets the interpretation of the message body or message body part of
	 * this ContentDispositionHeader.
	 * 
	 * @return interpretation of the message body or message body part
	 */
	public String getContentDisposition() {
		return this.encodeBody();
	}
}
/*
 * $Log: ContentDisposition.java,v $
 * Revision 1.4  2006/07/13 09:01:06  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  jeroen van bemmel
 * Reviewed by:   mranga
 * Moved some changes from jain-sip-1.2 to java.net
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 * Revision 1.3  2006/06/19 06:47:26  mranga
 * javadoc fixups
 *
 * Revision 1.2  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.1.1.1  2005/10/04 17:12:34  mranga
 *
 * Import
 *
 *
 * Revision 1.2  2004/01/22 13:26:29  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
