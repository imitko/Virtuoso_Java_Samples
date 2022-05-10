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
delete from tpcd..customer;
checkpoint;
tpcd..fill_customer(1, 5000);
tpcd..fill_customer(5001, 10000);
tpcd..fill_customer(10001, 15000);
tpcd..fill_customer(15001, 20000);
tpcd..fill_customer(20001, 25000);
tpcd..fill_customer(25001, 30000);
tpcd..fill_customer(30001, 35000);
tpcd..fill_customer(35001, 40000);
tpcd..fill_customer(40001, 45000);
tpcd..fill_customer(45001, 50000);
tpcd..fill_customer(50001, 55000);
tpcd..fill_customer(55001, 60000);
tpcd..fill_customer(60001, 65000);
tpcd..fill_customer(65001, 70000);
tpcd..fill_customer(70001, 75000);
tpcd..fill_customer(75001, 80000);
tpcd..fill_customer(80001, 85000);
tpcd..fill_customer(85001, 90000);
tpcd..fill_customer(90001, 95000);
tpcd..fill_customer(95001, 100000);
tpcd..fill_customer(100001, 105000);
tpcd..fill_customer(105001, 110000);
tpcd..fill_customer(110001, 115000);
tpcd..fill_customer(115001, 120000);
tpcd..fill_customer(120001, 125000);
tpcd..fill_customer(125001, 130000);
tpcd..fill_customer(130001, 135000);
tpcd..fill_customer(135001, 140000);
tpcd..fill_customer(140001, 145000);
tpcd..fill_customer(145001, 150000);
checkpoint;
