package com.mcd.plantation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Date;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "payment_transaction"
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentTransaction implements Serializable {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "payment_transaction_guid", length = 36, nullable = false)
    private String paymentTransactionGuid;

    @Column(name = "payment_transaction_number", nullable = false)
    private String paymentTransactionNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;
    
    @Column(name = "amount", nullable = false)
    private String amount;

    @Column(name = "payment_transaction_status")
    private String paymentTransactionStatus;

    @JdbcTypeCode(SqlTypes.JSON)
	@Column(name="pg_response_json")
    private String pgResponseJson;
    
    @Column(name = "pg_status_code")
    private String pgStatusCode;

    @Column(name = "pg_reciept_number")
    private String pgRecieptNumber;

    @Column(name = "doc_uri")
    private String docUri;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date", nullable = false, length = 29, updatable = false)
    private Date createdDate;

    @Column(name = "created_ip_addr", nullable = false)
    private String createdIpAddr;

    @Column(name = "created_remarks")
    private String createdRemarks;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "modified_ip_addr")
    private String modifiedIpAddr;

    @Column(name = "modified_remarks")
    private String modifiedRemarks;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    	
}