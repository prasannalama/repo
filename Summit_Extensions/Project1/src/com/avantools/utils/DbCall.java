/**
 * Copyright (C) 2009,2010 Sasha Stojanovic (sasha@avantools.com)
 *
 * DbCall - ADF to PL/SQL wrapper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.avantools.utils;

import java.sql.CallableStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Iterator;

import java.util.Map;

import oracle.jbo.JboException;
import oracle.jbo.server.DBTransaction;

/*
Few examples of "DbCall" in action:

Very simple procedure call with two IN parameters:
DbCall dc = new DbCall("MYPACKAGE.MYPROC", this.getDBTransaction());
dc.addIn("ABC");//add some carefully created string
dc.addIn(123);//add some integers too, as they are great partners to strings
dc.execute();//Call it here... Done!!!

Example of Procedure Call with various IN/OUT parameters:
DbCall dc = new DbCall("MYPACKAGE.MYPROC", this.getDBTransaction());
dc.addIn(this.getUserPrincipalName());//add the current user
dc.addIn(this.getMMH());//give a bit of mmh to you...
dc.addIn(new Timestamp(new java.util.Date().getTime()));//everybody needs some time
dc.addIn(null);//here you cannot go wrong!
dc.addOut("IS_SUMMER",Types.CHAR);//register this as OUT param as I need it later.
dc.addOut("HOW_HOT", Types.FLOAT);//I need this info too.
dc.execute();//call it here!!!
Object Summer = dc.getObj("IS_SUMMER");//and here is the summer!
Object Hot = dc.getObj("HOW_HOT");//here is how hot it is!

Example of Function Call with various IN/OUT parameters:
DbCall dc = new DbCall("?:=MYPACKAGE.MYFUNC", this.getDBTransaction());
dc.addRet("ret", Types.VARCHAR);//the function will return us something, here it goes
dc.addIn("ABC");//simple simple string as IN param
dc.addOut("RET", Types.FLOAT);//some OUT param, as I need to know this.
dc.addInOut(123,"WHAT",Types.NUMERIC);//here is very cool IN/OUT param
dc.execute();
Object ret = dc.getObj("RET");
Object what = dc.getObj("WHAT");
*/

public class DbCall {
    private class Par {
        protected final static int TypeIn = 1;
        protected final static int TypeOut = 2;
        protected final static int TypeInOut = 3;
        protected final static int TypeRet = 4;
        protected Object val = null;
        protected String key = null;
        protected int sqltype = 0;
        protected int parType = 0;

        protected Par(int parType, Object val, String key, int sqltype) {
            this.parType = parType;
            this.val = val;
            this.key = key;
            this.sqltype = sqltype;
        }
    }

    private CallableStatement cst;
    private int nReg = 0;
    private String statement = null;
    private HashMap hm = new HashMap();
    private DBTransaction dbt;
    private ArrayList<Par> ar = new ArrayList<Par>();

    Map<String, Object> returnMap = new HashMap<String, Object>();

    public DbCall(String statement, DBTransaction dbt) {
        this.statement = statement;
        this.dbt = dbt;
    }

    /**
     * add "in" statement parameter
     * @param ob
     */
    public void addIn(Object ob) {
        ar.add(new Par(Par.TypeIn, ob, null, 0));
    }

    /**
     * add "out" statement parameter and associate it with a key
     * after "execute" this value can be obtained by getObj function
     * @param key
     * @param sqltype
     */
    public void addOut(String key, int sqltype) {
        ar.add(new Par(Par.TypeOut, null, key, sqltype));
    }

    /**
     * add "in-out" statement parameter and associate it with a key
     * after "execute" this value can be obtained by getObj function
     * @param ob
     * @param key
     * @param sqltype
     */
    public void addInOut(Object ob, String key, int sqltype) {
        ar.add(new Par(Par.TypeInOut, ob, key, sqltype));
    }

    /**
     * associate function return with a key
     * after "execute" this value can be obtained by getObj function
     * @param key
     * @param sqltype
     */
    public void addRet(String key, int sqltype) {
        ar.add(new Par(Par.TypeRet, null, key, sqltype));
    }

    /**
     * Database Call. It will parse added parameters
     * in order to create correct statement and then do the actual call.
     * @return Map of key-value pairs containing all both return value (in case of functions) and output parameter values
     */
    public void execute() {
        try {
            executeEx();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new JboException(e);
        } finally {
            if (cst != null) {
                try {
                    cst.close();
                } catch (SQLException e) {
                    // nothing is needed
                }
            }
        }
    }
   
    /**
    * Use this to throw Exception outside
    */
    public void executeEx() throws SQLException {
        boolean hasReturnType = false; ////used in case of function calls
        if (ar != null) {
            String sAdd = "";
            for (int i = 0; i < ar.size(); i++) {
                Par par = ar.get(i);
                switch (par.parType) {
                case Par.TypeIn:
                case Par.TypeOut:
                case Par.TypeInOut:
                    sAdd += "?,";
                    break;
                case Par.TypeRet:
                    break;
                }
            }
            if (sAdd.length() > 0) {
                statement +=
                        "(" + sAdd.substring(0, sAdd.length() - 1) + ")";
            }
        }
        statement = "begin " + statement + "; end;";
        cst = dbt.createCallableStatement(statement, 0);
        if (ar != null) {
            for (int i = 0; i < ar.size(); i++) {
                Par par = ar.get(i);
                switch (par.parType) {
                case Par.TypeIn:
                    nReg++;
                    cst.setObject(nReg, par.val);
                    break;
                case Par.TypeOut:
                    nReg++;
                    cst.registerOutParameter(nReg, par.sqltype);
                    hm.put(par.key, nReg);
                    hasReturnType = true;
                    returnMap.put(par.key, null);
                    break;
                case Par.TypeInOut:
                    nReg++;
                    cst.setObject(nReg, par.val);
                    cst.registerOutParameter(nReg, par.sqltype);
                    hm.put(par.key, nReg);
                    hasReturnType = true;
                    returnMap.put(par.key, null);
                    break;
                case Par.TypeRet:
                    cst.registerOutParameter(++nReg, par.sqltype);
                    hm.put(par.key, nReg);
                    hasReturnType = true;
                    returnMap.put(par.key, null);
                    break;
                }
            }
        }
        cst.execute();
        if(hasReturnType) {
           
            Iterator<String> iterator = returnMap.keySet().iterator();
            while(iterator.hasNext()) {
                String key = iterator.next();
                returnMap.put(key, getRet(key));
            }
        }
    }
   

    /**
     * This method is returning the Return and Out parameter value. It should be used after the execute() method
     * @param key
     * @return
     */
    public Object getObj(String key) {
        return returnMap.get(key);
    }

    /**
     * After "execute" use this function to get value of parameter associated to key
     * @param key
     * @return
     */
    private Object getRet(String key) {
        try {
            return cst.getObject(Integer.parseInt(hm.get(key).toString()));
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new JboException(e);
        }

    }

}



