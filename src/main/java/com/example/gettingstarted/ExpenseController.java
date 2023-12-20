package com.example.gettingstarted;

import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.*;
import com.inrupt.client.webid.WebIdProfile;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.rdf.api.RDFSyntax;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Set;

@RequestMapping("/api")
@RestController
public class ExpenseController {

    /**
     * Note 1: Authenticated Session
     * Using the client credentials, create an authenticated session.
     */
    final Session session = OpenIdSession.ofClientCredentials(
            URI.create(System.getenv("MY_SOLID_IDP")).normalize(),
            System.getenv("MY_SOLID_CLIENT_ID"),
            System.getenv("MY_SOLID_CLIENT_SECRET"),
            System.getenv("MY_AUTH_FLOW"));
    /**
     * Note 2: SolidSyncClient
     * Instantiates a synchronous client for the authenticated session.
     * The client has methods to perform CRUD operations.
     */
    final SolidSyncClient client = SolidSyncClient.getClient().session(session);
    private final PrintWriter printWriter = new PrintWriter(System.out, true);

    /**
     * Note 3: SolidSyncClient.read()
     * Using the SolidSyncClient client.read() method, reads the user's WebID Profile document and returns the Pod URI(s).
     */
    @GetMapping("/pods")
    public Set<URI> getPods(@RequestParam(value = "webid", defaultValue = "") String webID) {
        printWriter.println("ExpenseController:: getPods");
        try (final var profile = client.read(URI.create(webID).normalize(), WebIdProfile.class)) {
            return profile.getStorages();
        }
    }

    /**
     * Note 4: SolidSyncClient.create()
     * Using the SolidSyncClient client.create() method,
     * - Saves the Expense as an RDF resource to the location specified in the Expense.identifier field.
     */
    @PostMapping(path = "/expenses/create")
    public Expense createExpense(@RequestBody Expense newExpense) {
        printWriter.println("ExpenseController:: createExpense");
        try (var createdExpense = client.create(newExpense)) {
            printExpenseAsTurtle(createdExpense);
            return createdExpense;
        } catch(PreconditionFailedException e1) {
            // Errors if the resource already exists
            printWriter.println(String.format("[%s] com.inrupt.client.solid.PreconditionFailedException:: %s", e1.getStatusCode(), e1.getMessage()));
        } catch(ForbiddenException e2) {
            // Errors if user does not have access to create
            printWriter.println(String.format("[%s] com.inrupt.client.solid.ForbiddenException:: %s", e2.getStatusCode(), e2.getMessage()));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Note 5: SolidSyncClient.read()
     * Using the SolidSyncClient client.read() method,
     * - Reads the RDF resource into the Expense class.
     */
    @GetMapping("/expenses/get")
    public Expense getExpense(@RequestParam(value = "resourceURL", defaultValue = "") String resourceURL) {
        printWriter.println("ExpenseController:: getExpense");
        try (var resource = client.read(URI.create(resourceURL).normalize(), Expense.class)) {
            return resource;
        } catch (NotFoundException e1) {
            // Errors if resource is not found
            printWriter.println(String.format("[%s] com.inrupt.client.solid.NotFoundException:: %s", e1.getStatusCode(), e1.getMessage()));
        } catch(ForbiddenException e2) {
            // Errors if user does not have access to read
            printWriter.println(String.format("[%s] com.inrupt.client.solid.ForbiddenException:: %s", e2.getStatusCode(), e2.getMessage()));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Note 6: SolidSyncClient.update()
     * Using the SolidSyncClient client.update() method,
     * - Updates the Expense resource.
     */
    @PutMapping("/expenses/update")
    public Expense updateExpense(@RequestBody Expense expense) {
        printWriter.println("ExpenseController:: updateExpense");

        try(var updatedExpense = client.update(expense)) {
            printExpenseAsTurtle(updatedExpense);
            return updatedExpense;
        } catch (NotFoundException e1) {
            // Errors if resource is not found
            printWriter.println(String.format("[%s] com.inrupt.client.solid.NotFoundException:: %s", e1.getStatusCode(), e1.getMessage()));
        } catch(ForbiddenException e2) {
            // Errors if user does not have access to read
            printWriter.println(String.format("[%s] com.inrupt.client.solid.ForbiddenException:: %s", e2.getStatusCode(), e2.getMessage()));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Note 7: SolidSyncClient.delete()
     * Using the SolidSyncClient client.delete() method,
     * - Deletes the resource located at the resourceURL.
     */

    @DeleteMapping("/expenses/delete")
    public void deleteExpense(@RequestParam(value = "resourceURL") String resourceURL) {
        printWriter.println("ExpenseController:: deleteExpense");
        try {
            client.delete(URI.create(resourceURL).normalize());

            // Alternatively, you can specify an Expense object to the delete method.
            // The delete method deletes  the Expense recorde located in the Expense.identifier field.
            // For example: client.delete(new Expense(URI.create(resourceURL).normalize()));
        } catch (NotFoundException e1) {
            // Errors if resource is not found
            printWriter.println(String.format("[%s] com.inrupt.client.solid.NotFoundException:: %s", e1.getStatusCode(), e1.getMessage()));
        } catch(ForbiddenException e2) {
            // Errors if user does not have access to read
            printWriter.println(String.format("[%s] com.inrupt.client.solid.ForbiddenException:: %s", e2.getStatusCode(), e2.getMessage()));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Note 9: Stores a non-RDF resource to a Pod
     *
     * Using SolidNonRDFSource and the SolidSyncClient .create() method,
     * - Saves a non-RDF resource at the destinationURL.
     */
    @PutMapping("/resource/nonRDF/add")
    public String addNonRDFFile(@RequestParam(value = "destinationURL") String destinationURL,
                                @RequestParam(value = "file") MultipartFile file) {
        printWriter.println("In addNonRDFFile:: Save Non-RDF File to Pod.");
        try (final var fileStream = file.getInputStream()) {

            SolidNonRDFSource myNonRDFFile = new SolidNonRDFSource(URI.create(destinationURL).normalize(), file.getContentType(), fileStream);
            return client.create(myNonRDFFile).getIdentifier().toString();
        } catch(PreconditionFailedException e1) {
            // Errors if the resource already exists
            printWriter.println(String.format("[%s] com.inrupt.client.solid.PreconditionFailedException in addNonRDFFile:: %s", e1.getStatusCode(), e1.getMessage()));
        } catch(ForbiddenException e2) {
            // Errors if user does not have access to create
            printWriter.println(String.format("[%s] com.inrupt.client.solid.ForbiddenException in addNonRDFFile:: %s", e2.getStatusCode(), e2.getMessage()));
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Note 10: Stores a non-RDF resource (image of the receipt) to a Pod and Attach to an Expense
     * Using methods defined as part of getting started, addReceiptToExpense:
     * - Calls addNonRDFFile() to store the receipt to a Pod
     * - Calls getExpense() to fetch the associated Expense RDF resource.
     * - Calls the Expense's setter `addReceipt` to add the link to the saved receipt.
     * - Calls updateExpense() to save the updated Expense.
     */
    @PutMapping("/expenses/receipts/add")
    public Expense addReceiptToExpense(@RequestParam(value = "destinationURL") String destinationURL,
                                       @RequestParam(value = "file") MultipartFile file,
                                       @RequestParam(value = "expenseURL") String expenseURL) {
        printWriter.println("In addReceiptToExpense: Save Receipt File to Pod and Update Associated Expense.");
        try {
            String receiptLocation = addNonRDFFile(destinationURL, file);
            if (receiptLocation != null) {
                Expense expense = getExpense(expenseURL);
                expense.addReceipt(receiptLocation);
                return updateExpense(expense);
            } else {
                printWriter.println("Error adding receipt");
                return null;
            }
        } catch(ForbiddenException e2) {
            // Errors if user does not have access to read or update the Expense resource
            printWriter.println(String.format("[%s] com.inrupt.client.solid.ForbiddenException in addReceiptToExpense:: %s", e2.getStatusCode(), e2.getMessage()));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Note 8: Prints the expense resource in Turtle.
     */

    private void printExpenseAsTurtle(Expense expense) {
        printWriter.println("ExpenseController:: printExpenseAsTurtle");
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        try  {
            expense.serialize(RDFSyntax.TURTLE, content);
            printWriter.println(content.toString("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
