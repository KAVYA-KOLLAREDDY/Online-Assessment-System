export enum Role {
  ADMIN = 'ADMIN',
  EXAMINER = 'EXAMINER',
  STUDENT = 'STUDENT'
}
export enum Status{
      PENDING ="PENDING",
	    APPROVED ="APPROVED",
	    REJECTED="REJECTED"
}

export interface Users {
  userId: number;
  name: string;
  email: string;
  password: string;
  phone: string;
  role: Role;
  isActive: boolean;
  isLocked: boolean;
  status:Status;
  dateOfBirth: string;
  createdAt: Date;
}
