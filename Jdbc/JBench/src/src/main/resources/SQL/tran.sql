--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
CREATE PROCEDURE ODBC_BENCHMARK
        @histid  int, 
        @acct    int, 
        @teller  int, 
        @branch  int, 
        @delta   float,
        @balance float output,
        @filler  char(22)
AS
BEGIN TRANSACTION
UPDATE    account
    SET   balance = balance + @delta
    WHERE account = @acct
SELECT    @balance = balance
    FROM  account 
    WHERE account = @acct
UPDATE    teller
    SET   balance = balance + @delta
    WHERE teller  = @teller
UPDATE    branch
    SET   balance = balance + @delta
    WHERE branch  = @branch
INSERT history
	(histid, account, teller, branch, amount, timeoftxn, filler)
VALUES
	(@histid, @acct, @teller, @branch, @delta, getdate(), @filler)
COMMIT TRANSACTION
