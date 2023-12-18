package com.schoolmgmtsys.root.ssg.models;

public class PaymentsModel {
    public Integer studentId;
    public Integer id;
    public String studentName;
    public String paymentTitle;
    public String paymentDesc;
    public String Amount;
    public String Status;
    public String Date;
    public String DueDate;
    public String PaidAmount;

    public PaymentsModel(Integer id, String studentName, String paymentTitle, String paymentDesc, String Amount, String Status, String Date, Integer studentId,String DueDate,String PaidAmount) {
        super();
        this.id = id;
        this.studentName = studentName;
        this.paymentTitle = paymentTitle;
        this.paymentDesc = paymentDesc;
        this.Amount = Amount;
        this.Status = Status;
        this.Date = Date;
        this.studentId = studentId;
        this.DueDate = DueDate;
        this.PaidAmount = PaidAmount;

    }
}
