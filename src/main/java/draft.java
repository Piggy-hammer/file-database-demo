import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class draft {
    public static void main(String[] args) {
        String drivername = "com.microsoft.sqlsever.jdbc.SQLSeverDriver";
        try {
            Class.forName(drivername);
            System.out.println("SUccess");
        }catch (ClassNotFoundException e){
            e.printStackTrace();
            System.out.println("fail");
        }
        String url ="jsbc:sqlserver://127.0.0.1:1433;DatabaseName=DESKTOP-3K3NO4G;user=DESKTOP-3K3NO4G/95328";
        try {
            Connection connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
