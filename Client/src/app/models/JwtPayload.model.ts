export interface JWTPayload {
    sub: string;
    username: string;
    exp: number;
    iat: number;
    authorities: string;
    iss: string;
}