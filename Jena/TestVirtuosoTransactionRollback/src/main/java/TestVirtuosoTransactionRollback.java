//package op.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import virtuoso.jdbc4.VirtuosoDataSource;
import virtuoso.jena.driver.VirtDataset;
import virtuoso.jena.driver.VirtModel;

public class TestVirtuosoTransactionRollback {

	public static final String test_model_A = "http://test/graphA";
	public static final String test_model_B = "http://test/graphB";
	public static final String test_model_C = "http://test/graphC";
	public static final String test_model_D = "http://test/graphD";

	public static void log(String mess) {
		System.out.println("   " + mess);
	}

	public static void main(String[] args) {
		try {
			Test_Txn1();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Test_Txn1() throws Exception {


				
		VirtuosoDataSource ds = new VirtuosoDataSource();
		ds.setDataSourceName("virtuoso");
		ds.setServerName("localhost");
		ds.setPortNumber(1111);
		ds.setUser("dba");
		ds.setPassword("dba");
		ds.setCharset("UTF-8");

		VirtDataset vd = null;
		VirtModel vm = null;
		

		try {
			
			vd = new VirtDataset(ds);
			Connection conn = vd.getConnection();
			conn.setAutoCommit(false);
			
			System.out.println("Start 1st transaction::(remove+insert 4 test graphs)");
			vd.begin();

            
			Model m = ModelFactory.createDefaultModel();
			m.add(m.createResource("http://test/person"), m.createProperty("http://test/hasAttribute"),
						m.createResource("http://test/lastName"));
			
			
			vd.removeNamedModel(test_model_A);	
			vd.removeNamedModel(test_model_B);
			vd.removeNamedModel(test_model_C);
			vd.removeNamedModel(test_model_D);
			
			vd.addNamedModel(test_model_A, m);
			vd.addNamedModel(test_model_B,m);
			vd.addNamedModel(test_model_C,m);
			vd.addNamedModel(test_model_D,m);

			addToPrivateGroup(conn, test_model_C);

			System.out.println("Chech if test_model_C is in private group:"+checkIfInPrivateGroup(conn, test_model_C));
			
			vd.commit();
			System.out.println("Committed 1st transaction");
			
			
			
			System.out.println("Checking if 4 test graphs exists in Virtuoso after commited 1st transaction::"
					+vd.containsNamedModel(test_model_A)+" "
					+vd.containsNamedModel(test_model_B)+" "
					+vd.containsNamedModel(test_model_C)+" "
					+vd.containsNamedModel(test_model_D));
			
			System.out.println("\n");
			
			System.out.println("Start 2nd transaction::(removal of 4 test graphs + removal of 1 test graph from private group)");
			
			vd.begin();
			
			vd.removeNamedModel(test_model_A);	
			vd.removeNamedModel(test_model_B);
			vd.removeNamedModel(test_model_C);
		
			removeFromPrivateGroup(conn, test_model_C); // ?? this implicitly calls the commit, so removal of  test_model_A, B and C is commited!!
			
			vd.removeNamedModel(test_model_D);
			
			System.out.println("Checking if test graph exists in Virtuoso before rollback() models removal::"
					+vd.containsNamedModel(test_model_A)+" "
					+vd.containsNamedModel(test_model_B)+" "
					+vd.containsNamedModel(test_model_C)+" "
					+vd.containsNamedModel(test_model_D));
			
			System.out.println("Chech if test_model_C is in private group:"+checkIfInPrivateGroup(conn, test_model_C));

			vd.abort();
			System.out.println("Rollback 2nd transaction");
			
			System.out.println("Checking if test graph exists in Virtuoso after rollback() of models removal:: "
					+vd.containsNamedModel(test_model_A)+" "
					+vd.containsNamedModel(test_model_B)+" "
					+vd.containsNamedModel(test_model_C)+" "
					+vd.containsNamedModel(test_model_D));
			

			System.out.println("Chech if test_model_C is in private group:"+checkIfInPrivateGroup(conn, test_model_C));
			
		} catch (Exception e) {
			System.err.println("ERROR Test Failed.");
			e.printStackTrace();
		} finally {
			try {
				if (vm != null)
					vm.close();
			} catch (Exception e) {
			}
			try {
				if (vd != null) {
					vd.close();
					vd.getConnection().close();
				}
			} catch (Exception e) {
			}
		}
	}
	
	 private static void removeFromPrivateGroup(Connection connection, String modelName) throws Exception {
		 String query = "DB.DBA.RDF_GRAPH_GROUP_DEL('http://www.openlinksw.com/schemas/virtrdf#PrivateGraphs','"+modelName+"')";
			final boolean execute = execute(connection, query);
			System.out.println("DB.DBA.RDF_GRAPH_GROUP_DEL executed="+execute);
	}
	 
	 private static void addToPrivateGroup(Connection connection, String modelName) throws Exception {
		 String query = "DB.DBA.RDF_GRAPH_GROUP_INS('http://www.openlinksw.com/schemas/virtrdf#PrivateGraphs','"+modelName+"')";
			final boolean execute = execute(connection, query);
			System.out.println("DB.DBA.RDF_GRAPH_GROUP_INS executed="+execute);
	}

	public static boolean execute(final Connection connection, final String query) throws Exception {
	        boolean success = false;

	        Statement preparedStatement = null;
	        try {
	            preparedStatement = connection.createStatement();
	            success = preparedStatement.execute(query);
	        } finally {
	        	preparedStatement.close();
	        }

	        return success;
	    }
	
	
	
	
	public static boolean checkIfInPrivateGroup(final Connection connection, String graphName) throws Exception {
		boolean found = false;
		Statement preparedStatement = null;
		try {
			preparedStatement = connection.createStatement();
			ResultSet rs = preparedStatement.executeQuery(
					"select __id2i(RGGM_MEMBER_IID) from RDF_GRAPH_GROUP_member where __id2i(RGGM_MEMBER_IID) like "
					+ "'"+ graphName + "'");
			if (rs.next()) {
				found = true;
			} else {
				found = false;
			}

		} finally {
			preparedStatement.close();
		}

		return found;
	}

}
