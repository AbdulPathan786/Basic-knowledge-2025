trigger AdmissionTrigger on Admission__c (after insert, after update) {
    InstallmentTrigger__c insRecord = InstallmentTrigger__c.getInstance();
    
    if(insRecord.isActive__c == true){
        if((Trigger.isInsert || Trigger.isUpdate) && Trigger.isAfter){
            AdmissionTriggerHandler.onAfterInsertUpdate(Trigger.new, Trigger.oldMap, Trigger.isUpdate, Trigger.isInsert);
        } 
    }
    
}
--------------------
public class AdmissionTriggerHandler{
     public static void onAfterInsertUpdate(List<Admission__c> admissions, Map<Id, Admission__c> admissionOldmap, Boolean isUpdate, Boolean isInsert){
         system.debug('@@@@ admissions ' + admissions);
         List<Installment__c> installMToInsert = new List<Installment__c>(); 
         Map<Id, Decimal> paidInstallments = new Map<Id, Decimal>();
         Map<Id, Decimal> paidCounts = new Map<Id, Decimal>();
         List<Admission__c> updatedAdms = new List<Admission__c>();
         
         if(isUpdate){
             for(Admission__c adm : admissions){
                if((adm.Total_Amount__c != NULL && adm.Total_Amount__c != admissionOldmap.get(adm.Id    ).Total_Amount__c) || (adm.Number_of_Installment__c != NULL && adm.Number_of_Installment__c != admissionOldmap.get(adm.Id).Number_of_Installment__c)){
                    updatedAdms.add(adm);
                }
             }
         }
         if(isUpdate && updatedAdms.size() > 0){
             DELETE [SELECT Id FROM Installment__c WHERE Admission__c IN : updatedAdms AND Status__c = 'Pending'];                
             
             for(AggregateResult ag : [SELECT Admission__c ad, SUM(Amount__c) totalPaid, COUNT(Id) paidCount FROM Installment__c WHERE 
                                       Admission__c IN : updatedAdms AND Status__c = 'Paid' GROUP By Admission__c]){
                 if(ag.get('ad') != NULL){
                     paidInstallments.put((ID)ag.get('ad'), (Decimal)ag.get('totalPaid'));
                     paidCounts.put((ID)ag.get('ad'), (Decimal)ag.get('paidCount'));
                 }
             }
         }
         
         for(Admission__c adm : admissions){
             System.debug('#####Adm ' + adm);
            if((isInsert || (isUpdate && updatedAdms.size() > 0))&& adm.Number_of_Installment__c != NULL && adm.Total_Amount__c != NULL){
                Decimal installments = isUpdate && paidCounts.containsKey(adm.Id) ? adm.Number_of_Installment__c - paidCounts.get(adm.Id) : adm.Number_of_Installment__c; 
                
                for(Integer im = 0; im < installments; im++){
                    Decimal totalAmount = isUpdate && paidInstallments.containsKey(adm.Id) ? adm.Total_Amount__c - paidInstallments.get(adm.Id)  : adm.Remaining_Amount__c;
                    Decimal amount = totalAmount / installments;
                    if((amount * installments) > totalAmount && im == installments-1){
                        amount = amount - ((amount * installments) - totalAmount);
                    }
                    if(adm.Admission_Date__c != NULL){
                        installMToInsert.add(new Installment__c(Amount__c = amount.setScale(2),
                                                              Admission__c = adm.Id,
                                                              Due_Date__c = adm.Admission_Date__c.addMonths(im),
                                                              //Payment_Method__c = loan.Payment_Method__c,
                                                              Status__c = 'Pending',
                                                              Ownerid = adm.ownerid));
                    }                                                              
                }
            }
        }
        system.debug('@@@@ installMToInsert' + installMToInsert);
        if(installMToInsert.size() > 0){
            INSERT installMToInsert;
        }
     }
}
--------------------