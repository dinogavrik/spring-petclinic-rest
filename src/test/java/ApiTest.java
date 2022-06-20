import io.restassured.RestAssured;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static io.restassured.RestAssured.given;

public class ApiTest {
	private static Connection connection;
	@BeforeAll
	public static void setConnection() throws SQLException {
		connection = DriverManager.getConnection(
			"jdbc:postgresql://localhost:5432/petclinic",
			"petclinic",
			"petclinic"
		);
	}

	@BeforeAll
	public static void setUpAuth() {

		PreemptiveBasicAuthScheme authScheme = new PreemptiveBasicAuthScheme();
		authScheme.setUserName("admin");
		authScheme.setPassword("admin");

		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

		RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

		RestAssured.requestSpecification = new RequestSpecBuilder()
			//.setBaseUri("localhost:8080")
			.setContentType(ContentType.JSON)
			.setAuth(authScheme)
			.build();


	}
	@AfterAll
	public static void closeConnection() throws SQLException {
		connection.close();
	}

	@Nested
	class Owner{

		@Test
		public void GetOwnersSuccess() throws SQLException {
			PreparedStatement sql = connection.prepareStatement("INSERT INTO owners( id, first_name, last_name, address, city, telephone) values ( 11, 'Lol', 'Lolov','10 Liberty St.' , 'Madison', '6085551089');");
			sql.executeUpdate();
			PreparedStatement sql1 = connection.prepareStatement("INSERT INTO pets( id,name, birth_date,owner_id) values ( 15, 'Lol', '2003-09-07', 11);");
			sql1.executeUpdate();

			given()
				.when()
				.get("/owners/11")
				.then()
				.statusCode(200)
				.body("id", is(11),
					"pets", notNullValue(),
					"pets[0].id", is(15));

			PreparedStatement sql2 = connection.prepareStatement("delete from pets where id = 15;");
			sql2.executeUpdate();
			PreparedStatement sql3 = connection.prepareStatement("delete from owners where id = 11;");
			sql3.executeUpdate();




		}
	}

}
