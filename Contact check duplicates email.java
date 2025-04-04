//CheckDuplicateContactEmail
trigger ContactTigger on Contact (before insert,before update) {
    Set<String> emailSet = new Set<String>();
    Map<String, Contact> existingEmailsMap = new Map<String, Contact>();
    Set<String> existingEmails = new Set<String>();
    
    if(trigger.isBefore && (trigger.isInsert || trigger.isUpdate)){
        for (Contact con : Trigger.new) {
            if (con.Email != null && con.Email.trim() != '') {
                emailSet.add(con.Email.trim().toLowerCase());
            }
        }
    } 
    
    if (!emailSet.isEmpty()) {
        for (Contact existingCon : [SELECT Email FROM Contact WHERE Email IN :emailSet]) {
            existingEmailsMap.put(existingCon.Email.trim().toLowerCase(), existingCon);
            existingEmails.add(existingCon.Email.trim().toLowerCase());
        }
    }
    
    for (Contact con : Trigger.new) {
        if (con.Email != null && existingEmails.contains(con.Email.trim().toLowerCase())) {
            //if (con.Email != null && existingEmailsMap.containsKey (con.Email.trim().toLowerCase())) {
            if (Trigger.isInsert || (Trigger.isUpdate && con.Email != Trigger.oldMap.get(con.Id).Email)) {
                con.Email.addError('Duplicate email address is not allowed..');
            }
        }
    }   
}
------------------------------------------------------
trigger ContactTrigger on Contact (before insert, before update) {
    Map<Id, Set<String>> accountEmailsMap = new Map<Id, Set<String>>();
    Map<Id, Set<String>> existingEmailsMap = new Map<Id, Set<String>>();
    
    for (Contact con : Trigger.new) {
        if (con.Email != null && con.Email.trim() != '') {
            Id accountId = con.AccountId;
            if (accountId != null) {
                if (!accountEmailsMap.containsKey(accountId)) {
                    accountEmailsMap.put(accountId, new Set<String>());
                }
                accountEmailsMap.get(accountId).add(con.Email.trim().toLowerCase());
            }
        }
    }
    
    for (Id accountId : accountEmailsMap.keySet()) {
        
        Set<String> emailsToCheck = accountEmailsMap.get(accountId);
        
        if (!emailsToCheck.isEmpty()) {
            for (Contact existingCon : [SELECT Email, AccountId FROM Contact WHERE AccountId = :accountId  AND Email IN :emailsToCheck ]) {
                if (!existingEmailsMap.containsKey(accountId)) {
                    existingEmailsMap.put(accountId, new Set<String>());
                }
                existingEmailsMap.get(accountId).add(existingCon.Email.trim().toLowerCase());
            }
        }
    }
    
    for (Contact con : Trigger.new) {
        if (con.Email != null && con.AccountId != null) {
            Set<String> existingEmails = existingEmailsMap.get(con.AccountId);
            
            if (existingEmails != null && existingEmails.contains(con.Email.trim().toLowerCase())) {
                if (Trigger.isInsert || (Trigger.isUpdate && con.Email != Trigger.oldMap.get(con.Id).Email)) {
                    con.Email.addError('Duplicate email address is not allowed for contacts under the same account.');
                }
            }
        }
    }
}
-----------END-------------------------------------