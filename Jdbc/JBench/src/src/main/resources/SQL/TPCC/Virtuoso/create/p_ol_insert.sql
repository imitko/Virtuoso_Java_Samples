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
create procedure ol_insert (
    inout w_id integer,
    inout d_id integer,
    inout o_id integer,
    in ol_number integer,
    inout ol_i_id integer,
    inout ol_qty integer,
    inout ol_amount float,
    inout ol_supply_w_id integer,
    inout ol_dist_info varchar,
    inout tax_and_discount float)
{
  if (ol_i_id = -1) return; 
  ol_amount := ol_amount * tax_and_discount;

  insert into tpcc..order_line (
      ol_o_id, ol_d_id, ol_w_id, ol_number, ol_i_id, ol_supply_w_id,
      ol_quantity, ol_amount, ol_dist_info) 
    values (
      o_id, d_id, w_id, ol_number, ol_i_id, ol_supply_w_id,
      ol_qty, ol_amount, ol_dist_info); 
}
