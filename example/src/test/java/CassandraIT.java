import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CassandraIT {

    @Test
    public void getAllServicesToRun() {
        Cluster cluster = null;
        try {
            cluster = Cluster.builder()
                    .addContactPoint("127.0.0.1")
                    .withPort(9042)
                    .build();
            Session session = cluster.connect();
            ResultSet rs = session.execute("select release_version from system.local");
            Row row = rs.one();
            String result = row.getString("release_version");
            System.out.println("Release Version: " + result);
            assertThat(result).contains("3.11");
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        finally {
            if (cluster != null){
                cluster.close();
            }
        }
    }


}
