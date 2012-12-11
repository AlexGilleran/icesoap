/**
 * 
 */
package com.alexgilleran.icesoap.parser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import com.alexgilleran.icesoap.exception.XMLParsingException;
import com.alexgilleran.icesoap.parser.IceSoapParser;
import com.alexgilleran.icesoap.parser.impl.IceSoapParserImpl;
import com.alexgilleran.icesoap.parser.test.xmlclasses.AddressChild;
import com.alexgilleran.icesoap.parser.test.xmlclasses.Alert;
import com.alexgilleran.icesoap.parser.test.xmlclasses.Booleans;
import com.alexgilleran.icesoap.parser.test.xmlclasses.NilValues;
import com.alexgilleran.icesoap.parser.test.xmlclasses.PipeTest;
import com.alexgilleran.icesoap.parser.test.xmlclasses.ProcessorTest;
import com.alexgilleran.icesoap.parser.test.xmlclasses.PurchaseOrder;
import com.alexgilleran.icesoap.parser.test.xmlclasses.Reply;

/**
 * @author Alex Gilleran
 * 
 */
public class IceSoapParserTest {
	private final static SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	public void testXsiNil() throws XMLParsingException, ParseException {
		IceSoapParser<NilValues> parser = new IceSoapParserImpl<NilValues>(NilValues.class);

		NilValues values = parser.parse(SampleXml.getNilValues());

		assertEquals(values.getCharValue(), '\0');
		assertEquals(values.getDoubleValue(), 0, 0);
		assertEquals(values.getFloatValue(), 0, 0);
		assertEquals(values.getIntValue(), 0);
		assertEquals(values.getLongValue(), 0);
		assertEquals(values.getStringValue(), null);
	}

	/**
	 * Holistic test on realistic data.
	 * 
	 * @throws XmlPullParserException
	 * @throws XMLParsingException
	 * @throws ParseException
	 */
	@Test
	public void testPurchaseOrder() throws XmlPullParserException, XMLParsingException, ParseException {
		IceSoapParser<PurchaseOrder> parser = new IceSoapParserImpl<PurchaseOrder>(PurchaseOrder.class);

		PurchaseOrder po = parser.parse(SampleXml.getPurchaseOrder());

		assertEquals(99503l, po.getPurchaseOrderNumber());
		assertEquals(FORMAT.parse("1999-10-20"), po.getOrderDate());

		// Shipping Address
		assertEquals("Shipping", po.getShippingAddress().getType());
		assertEquals("Ellen Adams", po.getShippingAddress().getName());
		assertEquals("123 Maple Street", po.getShippingAddress().getStreet());
		assertEquals("Mill Valley", po.getShippingAddress().getCity());
		assertEquals("CA", po.getShippingAddress().getState());
		assertEquals(10999, po.getShippingAddress().getZip());
		assertEquals("USA", po.getShippingAddress().getCountry());

		// Billing Address
		assertEquals("Billing", po.getBillingAddress().getType());
		assertEquals("Tai Yee", po.getBillingAddress().getName());
		assertEquals("8 Oak Avenue", po.getBillingAddress().getStreet());
		assertEquals("Old Town", po.getBillingAddress().getCity());
		assertEquals("PA", po.getBillingAddress().getState());
		assertEquals(95819, po.getBillingAddress().getZip());
		assertEquals("USA", po.getBillingAddress().getCountry());

		assertEquals("Please leave packages in shed by driveway.", po.getDeliveryNotes());

		// Item 1
		assertEquals("872-AA", po.getItem872aa().getPartNumber());
		assertEquals("Lawnmower", po.getItem872aa().getProductName());
		assertEquals(1d, po.getItem872aa().getQuantity(), 0d);
		assertEquals(new BigDecimal("148.95"), po.getItem872aa().getUsPrice());
		assertEquals(null, po.getItem872aa().getShipDate());
		assertEquals("Confirm this is electric", po.getItem872aa().getComment());

		// Item 2
		assertEquals("926-AA", po.getItem926aa().getPartNumber());
		assertEquals("Baby Monitor", po.getItem926aa().getProductName());
		assertEquals(2d, po.getItem926aa().getQuantity(), 0d);
		assertEquals(new BigDecimal("39.98"), po.getItem926aa().getUsPrice());
		assertEquals(FORMAT.parse("1999-05-21"), po.getItem926aa().getShipDate());
		assertEquals(null, po.getItem926aa().getComment());
	}

	/**
	 * Tests that when an object is passed in, both its fields and the fields of
	 * parent objects will be populated
	 * 
	 * @throws XMLParsingException
	 */
	@Test
	public void testInheritedFields() throws XMLParsingException {
		IceSoapParser<AddressChild> parser = new IceSoapParserImpl<AddressChild>(AddressChild.class);

		AddressChild address = parser.parse(SampleXml.getPurchaseOrder());

		// These should work no matter what
		assertEquals("CA", address.getState());
		assertEquals(10999, address.getZip());
		assertEquals("USA", address.getCountry());

		// If inheritance isn't working, these will fail.
		assertEquals("Shipping", address.getType());
		assertEquals("Ellen Adams", address.getName());
		assertEquals("123 Maple Street", address.getStreet());
		assertEquals("Mill Valley", address.getCity());

	}

	@Test
	public void testBooleans() throws XMLParsingException {
		IceSoapParser<Booleans> parser = new IceSoapParserImpl<Booleans>(Booleans.class);

		Booleans address = parser.parse(SampleXml.getBooleans());

		assertTrue(address.isAttribute());
		assertFalse(address.isFalseBoolean());
		assertTrue(address.isTrueBoolean());
		assertTrue(address.isUpperCaseBoolean());
		assertFalse(address.isTitleCaseBoolean());
	}

	@Test
	public void testProcessors() throws XMLParsingException {
		IceSoapParser<ProcessorTest> parser = new IceSoapParserImpl<ProcessorTest>(ProcessorTest.class);

		ProcessorTest testResult = parser.parse(SampleXml.getProcessorTest());

		assertEquals(SampleXml.TYPE_CONVERSION_VALUE, testResult.getConversionTest());
		assertEquals(SampleXml.CSV_CONVERSION_VALUE_1, testResult.getCsvTest()[0]);
		assertEquals(SampleXml.CSV_CONVERSION_VALUE_2, testResult.getCsvTest()[1]);
		assertEquals(SampleXml.CSV_CONVERSION_VALUE_3, testResult.getCsvTest()[2]);
	}

	@Test
	public void testListOfStrings() throws XMLParsingException {
		IceSoapParser<Alert> parser = new IceSoapParserImpl<Alert>(Alert.class);

		Alert testResult = parser.parse(SampleXml.getListOfStringsXML());

		assertEquals(1, testResult.getId());
		assertEquals("Jonas", testResult.getContact());
		assertEquals("some_email", testResult.getEmail());
		assertEquals("555-555555", testResult.getPhone());

		assertEquals("Fire", testResult.getActiveGroupsPerEmail().get(0));
		assertEquals(null, testResult.getActiveGroupsPerEmail().get(1));
		assertEquals("OpenClose", testResult.getActiveGroupsPerEmail().get(2));

		assertEquals(SampleXml.SMS_ALERT_GROUP_1, testResult.getActiveGroupsPerSMS().get(0));
		assertEquals(SampleXml.SMS_ALERT_GROUP_2, testResult.getActiveGroupsPerSMS().get(1));
	}

	@Test
	public void testCrappyLists() throws XMLParsingException, ParseException {
		IceSoapParser<Reply> parser = new IceSoapParserImpl<Reply>(Reply.class);

		Reply reply = parser.parse(SampleXml.getCrappyList());

		assertEquals(0, reply.exitTo);
		assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.sss").parse("2012-11-29T15:22:17.927"), reply.reqTime);
		assertEquals("USER_1", reply.users.get(0).name);
		assertEquals("USER_2", reply.users.get(1).name);
		assertEquals(1, reply.zones.get(0).id);
		assertEquals(2, reply.zones.get(1).id);
	}

	@Test
	public void testXPathUnionSimple() throws XMLParsingException {
		IceSoapParser<PipeTest> parser = new IceSoapParserImpl<PipeTest>(PipeTest.class);

		PipeTest pipeTest = parser.parse(SampleXml.getPipes1());
		assertEquals("value", pipeTest.getValue());

		pipeTest = parser.parse(SampleXml.getPipes2());
		assertEquals("value", pipeTest.getValue());
	}

}
