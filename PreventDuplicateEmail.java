trigger PreventDuplicateEmail on Contact (before insert, before update) {
    
    Set<String> newEmails = new Set<String>();  // Store emails from Trigger.new
    Set<String> duplicateEmails = new Set<String>();  // Track duplicates within the batch

    // Collect emails from Trigger.new to check duplicates in the same batch
    for (Contact con : Trigger.new) {
        if (con.Email != null) {
            String emailLower = con.Email.toLowerCase();
            
            if (newEmails.contains(emailLower)) {
                duplicateEmails.add(emailLower);  // Mark as duplicate in the batch
            } else {
                newEmails.add(emailLower);
            }
        }
    }

    // Query existing contacts in Salesforce
    Map<String, Id> existingEmails = new Map<String, Id>();
    for (Contact existingCon : [SELECT Id, Email FROM Contact WHERE Email IN :newEmails]) {
        existingEmails.put(existingCon.Email.toLowerCase(), existingCon.Id);
    }

    // Validate and show error for duplicates
    for (Contact con : Trigger.new) {
        String emailLower = con.Email != null ? con.Email.toLowerCase() : null;

        if (emailLower != null) {
            if (existingEmails.containsKey(emailLower)) {
                con.addError('Error: A contact with this email already exists in Salesforce!');
            }
            if (duplicateEmails.contains(emailLower)) {
                con.addError('Error: Duplicate email found within the same batch!');
            }
        }
    }
}