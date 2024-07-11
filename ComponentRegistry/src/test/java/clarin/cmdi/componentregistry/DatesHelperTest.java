/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.util.DatesHelper;
import java.text.ParseException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

import org.junit.Test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author olhsha
 */
public class DatesHelperTest {

    private final static Logger logger = LoggerFactory.getLogger(DatesHelperTest.class);

    private static final TimeZone TEST_TIME_ZONE = TimeZone.getTimeZone("Europe/Amsterdam");
    private static TimeZone origTimezone;

    @BeforeClass
    public static void beforeClass() {
        origTimezone = TimeZone.getDefault();
    }

    @Before
    public void beforeTest() {
        //Tests were written assuming the Europe/Amsterdam timezone
        logger.debug("Setting timezone to {}", TEST_TIME_ZONE);
        TimeZone.setDefault(TEST_TIME_ZONE);
    }

    @After
    public void afterTest() {
        logger.debug("Setting timezone to original {}", origTimezone);
        TimeZone.setDefault(origTimezone);
    }

    /**
     * Test of parseWorks method, of class DatesHelper.
     */
    @Test
    public void testParseWorks() throws ParseException {

        assertEquals(null, DatesHelper.parseWorks("Wrong date"));

        final String testDate = "2012-06-17T13:40:57+00:00";
        Date result = DatesHelper.parseWorks(testDate);
        assertFalse(null == result);

        final Calendar expected = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expected.set(2012, 5, 17, 13, 40, 57);
        final Instant expectedTime = expected.toInstant();

        assertEquals(expectedTime.getEpochSecond(), result.toInstant().getEpochSecond());
    }

    /**
     * Test of compareDateStrings method, of class DatesHelper.
     */
    @Test
    public void testCompareDateStrings() {
        String date1 = "2012-09-17T13:40:57+00:00";
        String date2 = "2012-09-17T13:40:58+00:00";

        int result1 = DatesHelper.compareDateStrings(date1, date2);
        assertEquals(1, result1);
        int result2 = DatesHelper.compareDateStrings(date2, date1);
        assertEquals(-1, result2);

    }

    /**
     * Test of getRFCDateTime method, of class DatesHelper.
     */
    @Test
    public void testGetRFCDateTime() {
        logger.debug("Timezone set to {}", TimeZone.getDefault());

        String dateString = "2012-09-17T13:40:57+00:00";
        String expResult = "Mon, 17 Sep 2012 15:40:57 +0200";
        String result = DatesHelper.getRFCDateTime(dateString);
        assertEquals(expResult, result);
    }

}
