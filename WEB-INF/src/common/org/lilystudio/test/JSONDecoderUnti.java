package org.lilystudio.test;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.lilystudio.coder.JSONDecoder;

public class JSONDecoderUnti {

  public Object testSimple(String cs) {
    try {
      return JSONDecoder.decode(cs);
    } catch (Exception ex) {
      return null;
    }
  }

  @Test
  public void test() throws Exception {
    testSimple("[{'name': 'ecui',        'type': 1, 'desc': 'ECUI 使用方式'}]");
    testSimple("[\"苹果\",82,\"梨\",117,\"香蕉\",231,\"桔子\",174,\"草莓\",201]");
    Assert.assertEquals(testSimple("\"a\\\"b\\nc\\t\\r\\b\\f\\x0a\\u000d\""), "a\"b\nc\t\r\b\f\n\r");
    Assert.assertNull(testSimple("\"\\\""));
    Assert.assertNull(testSimple("\"\\u231\""));
    Assert.assertNull(testSimple("\"\\xa\""));
    Assert.assertEquals(testSimple("23"), 23);
    Assert.assertEquals(testSimple("023"), 19);
    Assert.assertNull(testSimple("093"));
    Assert.assertNull(testSimple("023.3"));
    Assert.assertEquals(testSimple("0x23"), 35);
    Assert.assertNull(testSimple("0x23.3"));
    Assert.assertEquals(testSimple("23.05"), 23.05);
    Assert.assertNull(testSimple("."));
    Assert.assertNull(testSimple(".e2"));
    Assert.assertNull(testSimple("23.0.5"));
    Assert.assertNull(testSimple("23.05a"));
    Assert.assertNull(testSimple("23.05+"));
    Assert.assertEquals(testSimple(".0"), 0.0);
    Assert.assertEquals(testSimple("0."), 0.0);
    Assert.assertEquals(testSimple("0.e2"), 0.0e2);
    Assert.assertEquals(testSimple("23.05e2"), 23.05e2);
    Assert.assertEquals(testSimple("23.05e-2"), 23.05e-2);
    Assert.assertEquals(testSimple("0xFFFFFFFFFFFF"), 0xFFFFFFFFFFFFl);
    Assert.assertEquals(testSimple("-23"), -23);
    Assert.assertNull(testSimple("--2"));
    Assert.assertEquals(testSimple("-023"), -19);
    Assert.assertNull(testSimple("-093"));
    Assert.assertNull(testSimple("-023.3"));
    Assert.assertEquals(testSimple("-0x23"), -35);
    Assert.assertNull(testSimple("-0x23.3"));
    Assert.assertEquals(testSimple("-23.05"), -23.05);
    Assert.assertNull(testSimple("-."));
    Assert.assertNull(testSimple("-.e2"));
    Assert.assertNull(testSimple("-23.0.5"));
    Assert.assertNull(testSimple("-23.05a"));
    Assert.assertNull(testSimple("-23.05+"));
    Assert.assertEquals(testSimple("-.0"), 0.0);
    Assert.assertEquals(testSimple("-0."), 0.0);
    Assert.assertEquals(testSimple("-0.e2"), 0.0e2);
    Assert.assertEquals(testSimple("-23.05e2"), -23.05e2);
    Assert.assertEquals(testSimple("-23.05e-2"), -23.05e-2);
    Assert.assertEquals(testSimple("-0xFFFFFFFFFFFF"), -0xFFFFFFFFFFFFl);
    Assert.assertEquals(testSimple("  /*****asdf*****/  \"a\\\"b\\nc\\t\\r\\b\\f\\x0a\\u000d\""), "a\"b\nc\t\r\b\f\n\r");
    Assert.assertEquals(testSimple("  //asdfsd\n  \"a\\\"b\\nc\\t\\r\\b\\f\\x0a\\u000d\""), "a\"b\nc\t\r\b\f\n\r");
    Assert.assertNull(testSimple("null"));
    Assert.assertNull(testSimple("fals"));
    Assert.assertNull(testSimple("/*     * /"));
    Assert.assertEquals(testSimple("true"), true);
    Assert.assertEquals(testSimple("false"), false);
    Assert.assertEquals(testSimple("[true,false]"), Arrays.asList(new Boolean[] {true, false}));
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("true", true);
    map.put("number", 1);
    map.put("string", "s");
    map.put("null", null);
//    Assert.assertEquals(testSimple("{\"true\":true,\"number\":1,\"string\":\"s\",\"null\":null}"), map);
  }
}
