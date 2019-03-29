package com.junipersys.a3_chamber_test;

import android.content.Context;
import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

class QD_InsertParams{

    int ConfigOption;
    String strValue;

    QD_InsertParams(int ConfigOption, String strValue){
        this.ConfigOption = ConfigOption;
        this.strValue = strValue;
    }
}

class QD_Results {
    int workOrderCount;
    boolean isSN_assignedTo_WO;
    String PartNumber;
    boolean isPostTestPassed;
    int ErrorCode;
    String strException;

}

class QD_WriteToDatabase extends AsyncTask<QD_InsertParams,Void, QD_Results> {

    private Context mContext;
    private TaskCompleted mCallback;
    public QD_WriteToDatabase(Context context){
        this.mContext = context;
        this.mCallback = (TaskCompleted) context;
    }
    // JDBC driver name and database URL
    // static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:jtds:sqlserver://sqlserver.jsihome.com:1433/Juniper";

    //  Database credentials
    static final String USER = "QualityAdmin";
    static final String PASS = "JSys-Qdb1";

    String UnitItemID = "";
    QD_Results qd_results = new QD_Results();

    @Override
    protected QD_Results doInBackground(QD_InsertParams... params) {
        int ConfigOptionID = params[0].ConfigOption;
        String strValue = params[0].strValue;

        Connection conn = null;
        Statement stmnt = null;
        ResultSet rs = null;


        try {
            //Register JDBC Driver
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();

            //Open Connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            //Check Post Test Past

        }
        catch(Exception e)
        {

        }

        return qd_results;

    }

    private boolean QD_CheckChamber(){
        Timestamp testDate = null;
        Timestamp prevDate = new Timestamp(System.currentTimeMillis());
        Connection conn = null;
        Statement stmnt = null;
        ResultSet rs = null;

        try {
            //First get Item ID
            String sql = "EXECUTE Get_Items @SerialNumber = '" + "123456" + "'"; // TODO: Get SN from SP
            stmnt = conn.createStatement();
            rs = stmnt.executeQuery(sql);
            if (rs.next()) {
                UnitItemID = rs.getString(1);
            } else {
                return false;
            }

            rs.close();
            stmnt.close();


            //Check Final Config Stage
            sql = "Set_ItemTests @ItemID = " + UnitItemID + ", @TestID = 30, @TestStatusID = 126, @User = 138";
            stmnt = conn.createStatement();
            rs = stmnt.executeQuery(sql);

            rs.close();
            stmnt.close();

        } catch (Exception e) {
            String str = e.toString();

            return false;
        }

        return true;
    }
    @Override
    protected void onPreExecute(){

    }

    @Override
    protected void onPostExecute(QD_Results qd_results)
    {
        mCallback.onQD_TaskComplete(qd_results);
    }
}
