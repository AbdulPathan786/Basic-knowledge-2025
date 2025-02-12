trigger InstallmentTrigger on Installment__c (after insert, after update, after delete, after undelete) {
    InstallmentTrigger__c insRecord = InstallmentTrigger__c.getInstance();
    if(insRecord.isActive__c == true){
        InstallmentTriggerDispatcher.dispatch(Trigger.OperationType);
    } 
}
----------------------------------------------------------------------------
public class InstallmentTriggerDispatcher{
    public static void dispatch(System.TriggerOperation operationType){
        switch on operationType{
            WHEN AFTER_INSERT{
                InstallmentTriggerHandler.handleAfterInsert((Map<Id, Installment__c>)Trigger.newMap); // Map<Id, sObject>
            }
            WHEN AFTER_UPDATE{
                InstallmentTriggerHandler.handleAfterUpdate((Map<Id, Installment__c>)Trigger.newMap, (Map<Id, Installment__c>)Trigger.oldMap);
            }
            WHEN AFTER_DELETE{
                InstallmentTriggerHandler.handleAfterDelete((Map<Id, Installment__c>)Trigger.oldMap);
            }
            WHEN AFTER_UNDELETE{
                InstallmentTriggerHandler.handleAfterUndelete((Map<Id, Installment__c>)Trigger.newMap); // Map<Id, sObject>
            }
        }
    }
}
--------------------------------------------------------------------------
public class InstallmentTriggerHandler{

    public static void handleAfterInsert(Map<Id, Installment__c> newRecordsMap){
        InstallmentTriggerHelper.countInstallment(newRecordsMap);
    }
    public static void handleAfterUndelete(Map<Id, Installment__c> newRecordsMap){
        InstallmentTriggerHelper.countInstallment(newRecordsMap);
    }
    public static void handleAfterDelete(Map<Id, Installment__c> oldRecordsMap){
        InstallmentTriggerHelper.countInstallment(oldRecordsMap);
    }
    public static void handleAfterUpdate(Map<Id, Installment__c> newRecordsMap, Map<Id, Installment__c> oldRecordsMap){
        Set<Id> admissionIdsSet = new Set<Id>();
        List<Installment__c> updateAmounts = new List<Installment__c>();
        List<Installment__c> insertInstallments = new List<Installment__c>();
        
        for(Installment__c newRecord : newRecordsMap.values() ){            
            Installment__c oldRecord = oldRecordsMap.get(newRecord.Id);
            if(oldRecord.Admission__c != newRecord.Admission__c || newRecord.Amount__c != oldRecord.Amount__c || newRecord.Status__c != oldRecord.Status__c){
                admissionIdsSet.add(oldRecord.Admission__c);  
                admissionIdsSet.add(newRecord.Admission__c);  
            }
            if(oldRecord.Amount__c != newRecord.Amount__c && newRecord.Amount__c < oldRecord.Amount__c){                
                insertInstallments.add(new Installment__c(Admission__c = newRecord.Admission__c,
                                                         Amount__c = (oldRecord.Amount__c - newRecord.Amount__c).setScale(2),
                                                          Due_Date__c = oldRecord.Due_Date__c.addDays(15),
                                                          Status__c = 'Pending',
                                                          Ownerid = newRecord.ownerId));
            }
        }
        List<AggregateResult> aggregateList = [SELECT Sum(Amount__c), Admission__c 
                                               FROM Installment__c 
                                               WHERE Admission__c  IN:admissionIdsSet  AND 
                                               Status__c = 'Paid' 
                                               Group By Admission__c];

        List<Admission__c> admissionToUpdate = new List<Admission__c>();
        for(AggregateResult ag : aggregateList){
            String admissionId = (String)ag.get('Admission__c'); // Object
            Decimal totalIns = (Decimal)ag.get('expr0'); // Object
            Admission__c adm = new Admission__c();
            adm.Id = admissionId;
            adm.Paid_Amount__c = totalIns;
            admissionToUpdate.add(adm);
        }
        if(admissionToUpdate.size() > 0){
            UPDATE admissionToUpdate;        
        }
        if(insertInstallments.size() > 0){
            INSERT insertInstallments;
        }
    }
}
--------------------------------------------------------------------------------
public class InstallmentTriggerHelper {

    public static void countInstallment(Map<Id, Installment__c> newRecordsMap){

        Set<Id> admissionIdsSet = new Set<Id>();
        for(Installment__c ins : newRecordsMap.values()){
            if(ins.Admission__c != NULL){ // check if contact is related to account
                admissionIdsSet.add(ins.Admission__c);
            }
        }

        List<AggregateResult> aggregateList = [SELECT Sum(Amount__c), Admission__c
                                               FROM Installment__c 
                                               WHERE Admission__c IN : admissionIdsSet AND 
                                               Status__c = 'Paid' 
                                               Group By Admission__c];

        List<Admission__c> admissionToUpdate = new List<Admission__c>();
        for(AggregateResult ag : aggregateList){
            String admissionId = (String)ag.get('Admission__c'); // Object
            Decimal totalIns = (Decimal)ag.get('expr0'); // Object
            Admission__c adm = new Admission__c();
            adm.Id = admissionId;
            adm.Paid_Amount__c = totalIns;
            admissionToUpdate.add(adm);
        }
        if(admissionToUpdate.size() > 0){
            UPDATE admissionToUpdate;        
        }
    }
}
--------------------------------------------------------------------------------