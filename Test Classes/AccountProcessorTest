@isTest
public class AccountProcessorTest {

    @isTest
    static void testNoOfContacts() {

        Account a = new Account(Name = 'Test Account');
        insert a;

        Contact c1 = new Contact(
            FirstName = 'Bob',
            LastName = 'Willie',
            AccountId = a.Id
        );

        Contact c2 = new Contact(
            FirstName = 'Tom',
            LastName = 'Cruise',
            AccountId = a.Id
        );

        insert new List<Contact>{c1, c2};

        Test.startTest();

        AccountProcessor.countContacts(
            new List<Id>{a.Id}
        );

        Test.stopTest();

        Account acc = [
            SELECT Number_Of_Contacts__c
            FROM Account
            WHERE Id = :a.Id
        ];

        System.assertEquals(2, acc.Number_Of_Contacts__c);
    }
}

