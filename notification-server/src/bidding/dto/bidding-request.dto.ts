import { IsNumber, IsPositive } from 'class-validator';

export class BiddingRequestDto {
  @IsNumber()
  auctionId: number;

  @IsNumber()
  @IsPositive()
  amount: number;
}