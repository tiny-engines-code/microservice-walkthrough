package com.chrislomeli.mailermicroservice.bdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class StepDefinitions  {

    @Autowired
    private ApplicationContext applicationContext;

    BookStore store = new BookStore();

    boolean apiIsAvailable = true;
    int vendorIsReturning = 200;

    @Given("API is returning {int}")
    public void vaendorIsReturning(int status) {
        this.vendorIsReturning = status;
    }

    @Given("API is {available}")
    public void setApiIsAvailable(boolean available) {
        this.apiIsAvailable = available;
    }

    @Then("^print availablity")
    public void printAvailabiliy() {
        System.out.printf("AVIABILITY is %s%n", this.apiIsAvailable);
    }

    @Then("^print API returns status")
    public void printAPIReturn() {
        System.out.printf("AVIABILITY is %d%n", this.vendorIsReturning);
    }


    @Given("^I have the following books in the store by map$")
    public void haveBooksInTheStoreByMap(DataTable table) {

        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        for (Map<String, String> columns : rows) {
            store.addBook(new Book(columns.get("title"), columns.get("author")));
        }
        System.out.println("CHHES");
    }


    @When("^I search for books$")
    public void iSearch() {
        System.out.println("i SEARCH");
    }

    @Then("^I find all books$")
    public void iGet() {
        System.out.printf("Bookstore has %d books", store.allBooks().size());
    }


    @Given("today is Sunday")
    public void a_transaction_that_is_not_stored_in_our_system() {
        System.out.println("IN A TEST:: today is Sunda");
        assertThat(true).isTrue();
    }

    @When("I ask whether it's Friday yet")
    public void i_ask_wether_its_friday() {
        System.out.println("IN A TEST::I ask whether it's Friday yet");
        assertThat(true).isTrue();
    }

    @Then("I should be told Nope")
    public void i_should_be_told_nope() {
        System.out.println("IN A TEST:: NOPE");
        assertThat(true).isTrue();
    }

    @ParameterType("available|unavailable")
    public boolean available(String input) {
        return "available".equalsIgnoreCase(input);
    }

}