public class AccountHandler{
    public static Account insertNewAccount(String accountName){  
        Account acctToBeInserted = new Account(Name=accountName, AccountNumber='12345678');
      
        try{
            insert acctToBeInserted;
        }catch(DMLException e){
            System.debug('Inside DMLException catch ,error is ' + e.getMessage());
            acctToBeInserted = null;   
        }
               return acctToBeInserted;                 
       }
}
