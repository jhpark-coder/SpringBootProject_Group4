export class BiddingUpdateDto {
  success: boolean;
  message: string;
  auctionId: number;
  newHighestBid?: number;
  highestBidderName?: string;
}