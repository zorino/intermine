package org.flymine.objectstore.dummy;

import junit.framework.*;

import java.util.List;

import org.flymine.model.testmodel.*;
import org.flymine.objectstore.query.*;
import org.flymine.objectstore.ObjectStoreException;
import org.flymine.objectstore.ObjectStoreTestCase;
import org.flymine.testing.OneTimeTestCase;

public class ObjectStoreDummyImplTest extends ObjectStoreTestCase
{
    public ObjectStoreDummyImplTest(String arg) {
        super(arg);
    }

    public static Test suite() {
        return buildSuite(ObjectStoreDummyImplTest.class);
    }

    public static void oneTimeSetUp() throws Exception {
        ObjectStoreTestCase.oneTimeSetUp();

        os = new ObjectStoreDummyImpl();
        ((ObjectStoreDummyImpl) os).setExecuteTime(10);
        ((ObjectStoreDummyImpl) os).setMaxTime(20);
    }

    // Override setUpResults(). There are no results tests to do for an ObjectStoreDummyImpl.
    public static void setUpResults() throws Exception {
        // DO NOT SET UP ANY RESULTS
    }

    public void testAddRowRetrieveSame() throws Exception {

        ObjectStoreDummyImpl os = new ObjectStoreDummyImpl();
        ResultsRow row = new ResultsRow();
        String field1 = "test1";
        String field2 = "test2";
        row.add(field1);
        row.add(field2);

        os.addRow(row);
        os.setResultsSize(1);
        List rows = os.execute(new Query(), 0, 1);

        assertEquals(1, rows.size());
        ResultsRow newRow = (ResultsRow) rows.get(0);
        assertEquals(2, newRow.size());
        assertEquals("test1", (String) newRow.get(0));
        assertEquals("test2", (String) newRow.get(1));
    }

    public void testRetrieveNew() throws Exception {
        ObjectStoreDummyImpl os = new ObjectStoreDummyImpl();
        Query q = new Query();
        q.addToSelect(new QueryClass(String.class));
        q.addToSelect(new QueryClass(Department.class));

        os.setResultsSize(1);
        List rows = os.execute(q, 0, 1);

        assertEquals(1, rows.size());
        ResultsRow newRow = (ResultsRow) rows.get(0);
        assertEquals(2, newRow.size());
        assertTrue(newRow.get(0) instanceof String);
        assertTrue(newRow.get(1) instanceof Department);
    }

    public void testRetrieveNewAfterAdd() throws Exception {
        ObjectStoreDummyImpl os = new ObjectStoreDummyImpl();
        Query q = new Query();
        q.addToSelect(new QueryClass(String.class));
        q.addToSelect(new QueryClass(Department.class));

        ResultsRow row = new ResultsRow();
        String field1 = "test1";
        String field2 = "test2";
        row.add(field1);
        row.add(field2);

        os.addRow(row);
        os.setResultsSize(2);
        List rows = os.execute(q, 0, 2);

        assertEquals(2, rows.size());
        ResultsRow newRow = (ResultsRow) rows.get(0);
        assertEquals(2, newRow.size());
        assertEquals("test1", (String) newRow.get(0));
        assertEquals("test2", (String) newRow.get(1));
        newRow = (ResultsRow) rows.get(1);
        assertEquals(2, newRow.size());
        assertTrue(newRow.get(0) instanceof String);
        assertTrue(newRow.get(1) instanceof Department);
    }

    public void testRowLimit() throws Exception {
        ObjectStoreDummyImpl os = new ObjectStoreDummyImpl();
        Query q = new Query();
        os.setResultsSize(10);
        Results res = os.execute(q);

        assertEquals(10, res.size());

    }

    public void testReachEndOfResults() throws Exception {
        ObjectStoreDummyImpl os = new ObjectStoreDummyImpl();
        Query q = new Query();
        os.setResultsSize(10);
        Results res = os.execute(q);

        // Get the first 8 rows in a batch
        List rows = os.execute(q, 0, 8);
        assertEquals(8, rows.size());

        // Try and get the next 7
        rows = os.execute(q, 8, 7);
        assertEquals(2, rows.size());

        // Try and get rows 10 to 19
        rows = os.execute(q, 10, 10);
        assertEquals(0, rows.size());

        // Stupidly try and get beyond the end
        rows = os.execute(q, 15, 10);
        assertEquals(0, rows.size());
    }


    public void testExecuteCalls() throws Exception {
        ObjectStoreDummyImpl os = new ObjectStoreDummyImpl();
        Query q = new Query();
        os.setResultsSize(10);
        Results res = os.execute(q);
        os.execute(q, 1, 3);
        assertEquals(1, os.getExecuteCalls());
        os.execute(q, 5, 2);
        assertEquals(2, os.getExecuteCalls());
    }

    public void testPoisonRow() throws Exception {
        ObjectStoreDummyImpl os = new ObjectStoreDummyImpl();
        Query q = new Query();
        os.setResultsSize(10);
        os.setPoisonRowNo(7);
        os.execute(q, 0, 5);
        os.execute(q, 8, 2);
        os.execute(q, 4, 3);
        try {
            os.execute(q, 0, 10);
            fail("Expected: ObjectStoreException");
        } catch (ObjectStoreException e) {
        }
        try {
            os.execute(q, 7, 3);
            fail("Expected: ObjectStoreException");
        } catch (ObjectStoreException e) {
        }
        try {
            os.execute(q, 7, 1);
            fail("Expected: ObjectStoreException");
        } catch (ObjectStoreException e) {
        }
        try {
            os.execute(q, 0, 8);
            fail("Expected: ObjectStoreException");
        } catch (ObjectStoreException e) {
        }
    }

    public void testCount() throws Exception {
        ObjectStoreDummyImpl os = new ObjectStoreDummyImpl();
        os.setResultsSize(12);
        Query q = new Query();
        assertEquals(os.count(q), 12);
    }
}
